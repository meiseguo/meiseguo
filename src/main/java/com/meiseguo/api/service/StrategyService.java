package com.meiseguo.api.service;

import com.meiseguo.api.StrategyApi;
import com.meiseguo.api.pojo.*;
import com.meiseguo.api.strategy.*;
import com.meiseguo.api.utils.PagesUtil;
import com.meiseguo.api.vo.RecordVo;
import com.meiseguo.api.vo.StatisticVo;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

@Service
public class StrategyService implements StrategyApi {
    @Autowired
    private ConfigService configService;
    @Autowired
    private MongoTemplate mongoTemplate;
    public static final Map<String, Record> records = new ConcurrentHashMap<>();
    public static final Map<String, Record> markers = new ConcurrentHashMap<>();
    public static final Map<String, Relax> relaxMap = new ConcurrentHashMap<>();
    public static final Map<String, TimeLock> timeLocks = new ConcurrentHashMap<>();

    public List<Operator> operators() {
        return mongoTemplate.find(new Query(Criteria.where(Z.deleted.name()).is(0)), Operator.class);
    }

    public List<Account> accounts() {
        return mongoTemplate.find(new Query(Criteria.where(Z.deleted.name()).is(0)), Account.class);
    }

    public List<String> assets() {
        return mongoTemplate.find(new Query(Criteria.where(Z.deleted.name()).is(0)), Asset.class).stream().map(Asset::getCcy).distinct().collect(Collectors.toList());
    }

    public Record recover(String ccy) {
        List<Statistic> statistics = mongoTemplate.find(new Query(Criteria.where(Z.ccy.name()).is(ccy)).with(Sort.by(Z.sn.name()).descending()).limit(Record.LIMIT), Statistic.class);
        List<Input[]> history = statistics.stream().sorted(Comparator.comparing(Statistic::getSn)).skip(1).map(statistic -> new Input[]{
                new Input(statistic.ccy, statistic.from, statistic.start.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()),
                new Input(statistic.ccy, statistic.to, statistic.end.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()),
        }).collect(Collectors.toList());
        return new Record(history);
    }

    public List<Action> actions(String account, long timeMs) {
        timeLocks.computeIfAbsent(account, key -> new TimeLock(timeMs, key, 1));
        if (!timeLocks.get(account).locked(timeMs)) {
            return Collections.emptyList();
        }
        List<Action> actions = new ArrayList<>();
        List<Operator> all = allOperators(account);
        for (Operator op : all) {
            if (Mode.Freeze.name().equals(op.getMode())) {
                continue;
            }
            Strategy strategy = get(op);
            Record record = records.get(op.ccy);
            actions.addAll(strategy.apply(record.current()));
        }
        List<Action> result = actions.stream().filter(action -> action.amount > 0).collect(Collectors.toList());
        if (!result.isEmpty()) {
            mongoTemplate.insert(result, Action.class);
        }
        return result;
    }

    @Override
    public List<Operator> allOperators(String account) {
        return mongoTemplate.find(new Query(Criteria.where(Z.account.name()).is(account)), Operator.class);
    }


    @Override
    public Optional<Follow> getFollow(ActionRelation which, ObjectId sn) {
        return Optional.ofNullable(mongoTemplate.findOne(new Query(Criteria.where(which.name()).is(sn.toString())), Follow.class));
    }

    @Override
    public Optional<Operator> getPartner(Operator operator) {
        return Optional.ofNullable(mongoTemplate.findOne(new Query(Criteria.where(Z.operator.name()).is(operator.partner)), Operator.class));
    }

    @Override
    public Optional<Hedge> getHedge(ActionRelation which, ObjectId sn) {
        return Optional.ofNullable(mongoTemplate.findOne(new Query(Criteria.where(which.name()).is(sn.toString())), Hedge.class));
    }


    @Override
    public Relax relax(String operator, RelaxReason reason) {
        String key = operator + "." + reason.name();
        relaxMap.computeIfAbsent(key, k -> new Relax(operator, reason.desc()));
        return relaxMap.get(key);
    }

    @Override
    public Record record(String ccy) {
        return records.get(ccy);
    }

    @Override
    public Record marker(String ccy) {
        return markers.get(ccy);
    }

    @Override
    public Strategy get(Operator operator) {
        Setting setting = mongoTemplate.findOne(new Query(Criteria.where("setting").is(operator.setting).and("strategy").is(operator.strategy)), Setting.class);
        Safety safety = mongoTemplate.findOne(new Query(Criteria.where("setting").is(operator.setting).and("strategy").is(operator.strategy)), Safety.class);
        Asset asset = mongoTemplate.findOne(new Query(Criteria.where(Z.operator.name()).is(operator.operator).and(Z.ccy.name()).is(operator.ccy)), Asset.class);
        Status status = mongoTemplate.findOne(new Query(Criteria.where(Z.operator.name()).is(operator.operator)), Status.class);
        Account account = mongoTemplate.findOne(new Query(Criteria.where(Z.account.name()).is(operator.account)), Account.class);
        if(ObjectUtils.isEmpty(setting) || ObjectUtils.isEmpty(safety) || ObjectUtils.isEmpty(status) || ObjectUtils.isEmpty(asset) || ObjectUtils.isEmpty(account)){
            System.out.println(operator);
            alert("startup", operator, "严重问题，检查配置");
        }
        switch (operator.strategy) {
            case "Buy":
                return new Buy(operator, status, setting, safety, asset, account, this);
            case "Sell":
                return new Sell(operator, status, setting, safety, asset, account, this);
            default:
                throw new RuntimeException("wrong operator: " + operator);
        }
    }

    @Override
    public Action get(String sn) {
        Action action = mongoTemplate.findOne(new Query(Criteria.where(Z.sn.name()).is(new ObjectId(sn))), Action.class);
        assert action != null;
        return action;
    }

    @Override
    public List<Action> pendingActions(Operator operator, StrategyType type) {
        Record record = record(operator.ccy);
        List<Action> pending = mongoTemplate.find(new Query(Criteria.where(Z.operator.name()).is(operator.operator).and(Z.account.name()).is(operator.account).and(Z.ccy.name()).is(operator.ccy).and("type").is(type).and(Z.status.name()).in(ActionStatus.init.name(), ActionStatus.live.name())), Action.class);
        // 超时就标记为取消
        pending.stream()
                .filter(action -> {
                    if (ActionStatus.init.name().equals(action.status) && record.current.millis - action.millis > TimeUnit.SECONDS.toMillis(120)) {
                        return true;
                    }
                    return ActionStatus.live.name().equals(action.status) && record.current.millis - action.millis > TimeUnit.SECONDS.toMillis(360);
                }).forEach(action -> {
                    action.setStatus(ActionStatus.canceled.name());
                    action.setUpdatetime(LocalDateTime.now());
                    save(action);
                });
        return pending.stream().filter(action -> !ActionStatus.canceled.name().equals(action.status)).collect(Collectors.toList());
    }

    @Override
    public List<Action> actionList(Operator operator, StrategyType type, String side, String status) {
        return mongoTemplate.find(new Query(Criteria.where(Z.operator.name()).is(operator.operator).and(Z.account.name()).is(operator.account).and(Z.ccy.name()).is(operator.ccy).and("type").is(type.name()).and("side").is(side).and(Z.status.name()).is(status)), Action.class);
    }

    @Override
    public List<Follow> followList(Operator operator, StrategyType type, String side, String status) {
        return mongoTemplate.find(new Query(Criteria.where(Z.operator.name()).is(operator.operator).and(Z.ccy.name()).is(operator.ccy).and(Z.type.name()).is(type.name()).and("side").is(side).and(Z.status.name()).is(status)), Follow.class);
    }

    @Override
    public Optional<Hedge> nextHedge(Operator operator, StrategyType type, String status) {
        return Optional.ofNullable(mongoTemplate.findOne(new Query(Criteria.where(Z.operator.name()).is(operator.operator).and(Z.type.name()).is(type.name()).and(Z.status.name()).is(status)), Hedge.class));
    }

    @Override
    public Optional<Closed> lastClosed(Operator operator, StrategyType type) {
        return Optional.ofNullable(mongoTemplate.findOne(new Query(Criteria.where(Z.operator.name()).is(operator.operator).and(Z.type.name()).is(type.name())).with(Sort.by("createtime").descending()).limit(1), Closed.class));
    }

    @Override
    public void update(String ordId, String sn, String status) {
        Action action = mongoTemplate.findOne(new Query(Criteria.where(Z.sn.name()).is(new ObjectId(sn))), Action.class);
        assert action != null;
        Optional<String> debug = configService.get("order.debug");
        if (debug.isPresent() && debug.get().equals("true")) {
            if (ActionStatus.error.name().equals(status)) {
                ordId = sn;
            }
            status = ActionStatus.filled.name();
        }
        System.out.println(ordId + ", status: " + status + ", sn: " + sn);
        if (ActionStatus.error.name().equals(status)) {
            long count = mongoTemplate.count(new Query(Criteria.where(Z.order.name()).is(ordId)), Action.class);
            if (count > 1) {
                mongoTemplate.remove(new Query(Criteria.where(Z.order.name()).is(ordId)), Action.class);
            }
            relax(action.operator, RelaxReason.open).calm(TimeUnit.HOURS.toSeconds(1));
        }
        action.setOrder(ordId);
        action.setStatus(status);
        action.setUpdatetime(LocalDateTime.now());
        mongoTemplate.save(action);
        // 更新跟单状态
        Optional<Follow> follow = getFollow(ActionRelation.close, action.getSn());
        if (follow.isPresent()) {
            Follow todo = follow.get();
            todo.setStatus(status);
            todo.setValue(todo.getAmount() * (StrategyType.buy.name().equals(todo.type) ? (action.getPrice() - todo.getPrice()) : (todo.getPrice() - action.getPrice())));
            save(todo);
        }
        // 更新对冲单状态
        Optional<Hedge> hedge = getHedge(ActionRelation.close, action.getSn());
        if (hedge.isPresent()) {
            Hedge todo = hedge.get();
            todo.setStatus(status);
            save(todo);
        }
    }

    @Override
    public void save(Action action) {
        action.setUpdatetime(LocalDateTime.now());
        mongoTemplate.save(action);
    }

    @Override
    public void save(Closed close) {
        mongoTemplate.save(close);
    }

    @Override
    public void save(Safety safety) {
        safety.setUpdatetime(LocalDateTime.now());
        mongoTemplate.save(safety);
    }

    @Override
    public void ticker(String ccy, double price, long ts) {
        records.computeIfAbsent(ccy, this::recover);
        markers.computeIfAbsent(ccy, key -> new Record(3, new LinkedList<>(), 0.002));
        Input input = new Input(ccy, price, ts);
        markers.get(ccy).apply(input);
        records.get(ccy).apply(input).ifPresent(statistic -> mongoTemplate.save(statistic));
    }

    @Override
    public void save(Guess guess) {
        if (!guess.getList().isEmpty()) {
            mongoTemplate.remove(new Query(Criteria.where(Z.operator.name()).is(guess.getOperator()).and(Z.traceid.name()).ne(guess.getTraceId())), Case.class);
            mongoTemplate.insert(guess.getList(), Case.class);
            guess.getList().clear();
        }
    }

    @Override
    public void save(Hedge hedge) {
        hedge.setUpdatetime(LocalDateTime.now());
        mongoTemplate.save(hedge);
    }

    @Override
    public void save(Follow follow) {
        follow.setUpdatetime(LocalDateTime.now());
        mongoTemplate.save(follow);
    }

    @Override
    public void save(Operator operator) {
        operator.setUpdatetime(LocalDateTime.now());
        mongoTemplate.save(operator);
    }

    @Override
    public void save(Status status) {
        status.setUpdatetime(LocalDateTime.now());
        mongoTemplate.save(status);
    }

    @Override
    public void save(Setting setting) {
        setting.setUpdatetime(LocalDateTime.now());
        mongoTemplate.save(setting);
    }

    @Override
    public void stopLoss(Operator operator, Setting setting) {
        // 浮亏扩大，设置止损状态
        if (operator.stopLoss == 0) {
            relax(operator.operator, RelaxReason.loss).calm(TimeUnit.HOURS.toSeconds(12));
            operator.setStopLoss(1);
            operator.setOpenAmt(setting.unitAmt);
            alert("stopLoss", operator, "止损半天，投资降级：" + operator.openAmt);
            save(operator);
        }

        Optional<Operator> partner = getPartner(operator);
        if(partner.isPresent()) {
            Operator op = partner.get();
            if (op.stopLoss == 1) {
                relax(op.operator, RelaxReason.open).calm(setting.timeGapLoss);
                alert("stopLoss", op, "止损结束");
                op.setStopLoss(0);
                save(op);
            }
        }
    }

    public long liqPx(String account, String instId, String instType, String mgnMode, double price, double average) {
        Query query = new Query(Criteria.where("tdMode").is(mgnMode).and(Z.account.name()).is(account).and(Z.ccy.name()).is(instId).and("instType").is(instType));
        Update update = Update.update("danger", price).set(Z.updatetime.name(), LocalDateTime.now()).set("average", average);
        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, Safety.class);
        return updateResult.getModifiedCount();
    }

    @Override
    public void alert(String from, Operator operator, String message) {
        Optional<Warn> latest = Optional.ofNullable(mongoTemplate.findOne(new Query(Criteria.where("operator").is(operator.operator).and("status").is("new")).with(Sort.by(Sort.Order.desc("sn"))).limit(1), Warn.class));
        if (latest.isPresent()) {
            Warn alert = latest.get();
            if (message.equals(alert.message)) {
                alert.setUpdatetime(LocalDateTime.now());
                alert.setTimes(alert.times + 1);
                mongoTemplate.save(alert);
                return;
            }
        }
        Warn alert = new Warn(operator);
        alert.setMessage(message);
        alert.setTrigger(from);
        mongoTemplate.save(alert);
    }

    public void report(String account, Order order) {
        if (!ObjectUtils.isEmpty(order.getClOrdId())) {
            update(order.getOrdId(), order.getClOrdId(), order.getState());
            return;
        }
        Order old = mongoTemplate.findOne(new Query(Criteria.where("ordId").is(order.getOrdId())), Order.class);
        if (old != null) {
            order.setSn(old.getSn());
        }
        order.setAccount(account);
        System.out.println("手动订单上报");
        mongoTemplate.save(order);
    }

    public RecordVo data(String ccy) {
        Record online = records.get(ccy);
        List<Statistic> statistics = mongoTemplate.find(new Query(Criteria.where(Z.ccy.name()).is(ccy)), Statistic.class);
        List<Input[]> history = statistics.stream().map(statistic -> new Input[]{
                new Input(statistic.ccy, statistic.from, statistic.start.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()),
                new Input(statistic.ccy, statistic.to, statistic.end.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()),
        }).collect(Collectors.toList());
        Record record = new Record(history);
        record.apply(new Input(ccy, online.current.price, online.current.millis));
        return getRecordVo(record);
    }

    public StatisticVo statistic(String ccy) {
        StatisticVo statisticVo = new StatisticVo();
        Record record = records.get(ccy);
        statisticVo.setRecordVo(getRecordVo(record));
        Record marker = markers.get(ccy);
        statisticVo.setMarkerVo(getRecordVo(marker));
        if(record == null || marker == null || record.current == null || marker.current == null){
            System.out.println("还未上报数据: records=" + records + ", markers=" + markers);
            return statisticVo;
        }
        List<Action> list = new ArrayList<>();
        List<Action> pendingBuy = mongoTemplate.find(new Query(Criteria.where("side").is(OrderSide.buy.name()).and("type").is(StrategyType.buy.name()).and(Z.ccy.name()).is(ccy).and(Z.status.name()).is(ActionStatus.filled.name())), Action.class);
        List<Action> pendingSell = mongoTemplate.find(new Query(Criteria.where("side").is(OrderSide.sell.name()).and("type").is(StrategyType.sell.name()).and(Z.ccy.name()).is(ccy).and(Z.status.name()).is(ActionStatus.filled.name())), Action.class);
        list.addAll(pendingBuy);
        list.addAll(pendingSell);
        list.sort(Comparator.comparing(Action::getCreatetime));
        double[] buy = list.stream().mapToDouble(a -> {
            if (StrategyType.buy.name().equals(a.getType())) return a.winRatio(record.current);
            return 0;
        }).toArray();
        double[] sell = list.stream().mapToDouble(a -> {
            if (StrategyType.sell.name().equals(a.getType())) return a.winRatio(record.current);
            return 0;
        }).toArray();
        double[] buyD = list.stream().mapToDouble(a -> {
            if (StrategyType.buy.name().equals(a.getType())) return a.turnRatio(record.current);
            return 0;
        }).toArray();
        double[] sellD = list.stream().mapToDouble(a -> {
            if (StrategyType.sell.name().equals(a.getType())) return a.turnRatio(record.current);
            return 0;
        }).toArray();

        List<String> x = list.stream().map(a -> a.getCreatetime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))).collect(Collectors.toList());
        statisticVo.setBuy(buy);
        statisticVo.setSell(sell);
        statisticVo.setBuyD(buyD);
        statisticVo.setSellD(sellD);
        statisticVo.setXAxis(x);

        // 浮亏
        double sum = list.stream().mapToDouble(a -> a.winRatio(record.current) * a.amount * a.price).sum();
        double s = list.stream().filter(a -> StrategyType.sell.name().equals(a.type)).mapToDouble(Action::getValue).sum();
        double b = list.stream().filter(a -> StrategyType.buy.name().equals(a.type)).mapToDouble(Action::getValue).sum();

        List<Closed> closedList = mongoTemplate.find(new Query(Criteria.where(Z.deleted.name()).is(0).and(Z.ccy.name()).is(ccy)), Closed.class);
        double win = closedList.stream().mapToDouble(Closed::getValue).sum();
        statisticVo.setInvest(new double[]{Math.abs(sum), b, s, Math.abs(win)});
        statisticVo.setPie(Arrays.asList("浮动盈亏" + PagesUtil.numbers(sum), "多" + PagesUtil.numbers(b), "空" + PagesUtil.numbers(s), "实现收益" + PagesUtil.numbers(win)));

        Map<String, List<Closed>> dateClosed = closedList.stream().collect(Collectors.groupingBy(c -> c.getUpdatetime().format(DateTimeFormatter.ofPattern("MM/dd"))));
        List<String> collect = closedList.stream().filter(c -> c.getUpdatetime().isAfter(LocalDateTime.now().minusDays(7))).map(c -> c.getUpdatetime().format(DateTimeFormatter.ofPattern("MM/dd"))).distinct().collect(Collectors.toList());
        double[] ptdClosed = collect.stream().mapToDouble(date -> {
            List<Closed> closedDate = dateClosed.get(date);
            return closedDate.stream().mapToDouble(Closed::getValue).sum();
        }).toArray();

        List<Closed> todayList = closedList.stream().filter(c -> LocalDate.now().atStartOfDay().isBefore(c.getUpdatetime())).collect(Collectors.toList());
         List<String> today = todayList.stream().map(c -> c.getUpdatetime().format(DateTimeFormatter.ofPattern("HH:mm"))).collect(Collectors.toList());

        double[] todayBuy = todayList.stream().mapToDouble(c -> {
            if (StrategyType.buy.name().equals(c.getType())) {
                return c.getValue();
            } else return 0;
        }).toArray();
        double[] todaySell = todayList.stream().mapToDouble(c -> {
            if (StrategyType.sell.name().equals(c.getType())) {
                return c.getValue();
            } else return 0;
        }).toArray();
        double[] todayB = todayList.stream().mapToDouble(c -> {
            if (StrategyType.buy.name().equals(c.getType())) {
                return c.getRatio();
            } else return 0;
        }).toArray();
        double[] todayS = todayList.stream().mapToDouble(c -> {
            if (StrategyType.sell.name().equals(c.getType())) {
                return c.getRatio();
            } else return 0;
        }).toArray();
        statisticVo.setBuyToday(todayBuy);
        statisticVo.setSellToday(todaySell);
        statisticVo.setTodayBuy(todayB);
        statisticVo.setTodaySell(todayS);
        statisticVo.setWinToday(today);
        statisticVo.setSumToday("累计：" + PagesUtil.numbers(todayList.stream().mapToDouble(Closed::getValue).sum()));
        statisticVo.setTodayBS("做多：" + PagesUtil.money(DoubleStream.of(todayBuy).filter(d -> d<0).sum()) + "+" + PagesUtil.money(DoubleStream.of(todayBuy).filter(d -> d>0).sum())+ " 做空：" + PagesUtil.money(DoubleStream.of(todaySell).filter(d -> d<0).sum()) + "+" + PagesUtil.money(DoubleStream.of(todaySell).filter(d -> d>0).sum()));
        statisticVo.setRatioBS("做多：" + DoubleStream.of(todayB).filter(d -> d>0).count() + "-" + DoubleStream.of(todayB).filter(d -> d<0).count() + " 做空：" + DoubleStream.of(todayS).filter(d -> d>0).count() + "-" + DoubleStream.of(todayS).filter(d -> d<0).count());
        statisticVo.setXClosed(collect);
        statisticVo.setPtdClosed(ptdClosed);

        List<Input[]> prices = new ArrayList<>(record.lowHigh);
        List<Series> series = new ArrayList<>();
        Series series1 = new Series("line", "价格波动", prices.stream().flatMap(Arrays::stream).mapToDouble(Input::getPrice).toArray(), "none");
        List<Safety> safeties = mongoTemplate.find(new Query(Criteria.where(Z.deleted.name()).is(0).and(Z.ccy.name()).is(ccy)), Safety.class);
        double min = Arrays.stream(series1.getData()).min().orElse(1);
        double max = Arrays.stream(series1.getData()).max().orElse(0.0);
        for (Safety safety : safeties) {
            if ("SPOT".equals(safety.getInstType())) {
                if (StrategyType.buy.name().equals(safety.getType())) {
                    double aveBuy = pendingBuy.stream().mapToDouble(Action::getPrice).average().orElse(record.current.price);
                    safety.setAverage(aveBuy);
                    save(safety);
                } else {
                    double aveSell = pendingSell.stream().mapToDouble(Action::getPrice).average().orElse(record.current.price);
                    safety.setAverage(aveSell);
                    save(safety);
                }
            }
            Series series3 = new Series("line", safety.setting, Arrays.stream(series1.getData()).map(p -> safety.average).toArray(), "none");
            series.add(series3);
            if (safety.average < min) min = safety.average;
            if (safety.average > max) max = safety.average;
        }
        AtomicInteger i = new AtomicInteger();
        int[] counter = prices.stream().flatMap(Arrays::stream).mapToInt(p -> i.incrementAndGet()).toArray();
        series.add(series1);
        List<String> legend = series.stream().map(Series::getName).collect(Collectors.toList());
        statisticVo.setSeries(series);
        statisticVo.setCount(counter);
        statisticVo.setLegend(legend);
        statisticVo.setHigh(max);
        statisticVo.setLow(min);
        statisticVo.setCurrent(record.current.price);
        statisticVo.setDesc(record.nextTimes(StrategyType.buy));
        statisticVo.setAsc(record.nextTimes(StrategyType.sell));
        long timeLeft = record.timeLeft();
        statisticVo.setTimeout(TimeUnit.MILLISECONDS.toMinutes(timeLeft) + "分钟" + TimeUnit.MILLISECONDS.toSeconds(timeLeft) % 60 + "秒" + "多：" + PagesUtil.numbers(b) + " 空：" + PagesUtil.numbers(s));
        return statisticVo;
    }

    private RecordVo getRecordVo(Record record) {
        RecordVo statisticVo =  new RecordVo();
        if (record == null) {
            return statisticVo;
        }
        List<Input[]> temp = new ArrayList<>(record.lowHigh);
        temp.add(new Input[0]);
        temp.add(new Input[0]);
        double[] up = temp.stream().mapToDouble(inputs -> {
            if (inputs == null || inputs.length == 0) return 0;
            if (inputs[0].price <= inputs[1].price) return (inputs[1].price - inputs[0].price) / inputs[0].price;
            return 0;
        }).toArray();

        double[] down = temp.stream().mapToDouble(inputs -> {
            if (inputs == null || inputs.length == 0) return 0;
            if (inputs[0].price > inputs[1].price) return (inputs[0].price - inputs[1].price) / inputs[0].price;
            return 0;
        }).toArray();
        String pattern = "MM/dd HH:mm";
        statisticVo.setUp(up);
        statisticVo.setRsi((int)record.rsi());
        statisticVo.setDown(down);
        List<String> index = temp.stream().map(inputs -> {
            if (inputs == null || inputs.length == 0) return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
            return Instant.ofEpochMilli(inputs[0].getMillis()).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(pattern));
        }).collect(Collectors.toList());
        statisticVo.setIndex(index);
        double[] aveU = temp.stream().mapToDouble(x -> record.average(1)).toArray();
        double[] aveD = temp.stream().mapToDouble(x -> record.average(-1)).toArray();
        statisticVo.setDownAverage(aveD);
        statisticVo.setUpAverage(aveU);
        double[] latest = temp.stream().mapToDouble(x -> 0).toArray();
        double[] bounce = temp.stream().mapToDouble(x -> 0).toArray();
        latest[latest.length - 2] = record.latest();
        bounce[bounce.length - 1] = record.decline();
        statisticVo.setLatest(latest);
        statisticVo.setBounce(bounce);
        statisticVo.setUpDown(record.latest());
        statisticVo.setGoBack(record.decline());
        return statisticVo;
    }
}
