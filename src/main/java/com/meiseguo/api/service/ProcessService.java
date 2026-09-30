package com.meiseguo.api.service;

import com.meiseguo.api.API;
import com.meiseguo.api.dto.PageDto;
import com.meiseguo.api.dto.UpdateDto;
import com.meiseguo.api.pojo.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.ObjectUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ProcessService implements IManageService {
    public static final List<Proc> procList = new ArrayList<>();
    private MongoTemplate mongoTemplate;
    private ExecutorService threadPool;

    public void restart() {
        System.out.println("正在重启tickers");
        Config configTickers = mongoTemplate.findOne(new Query(Criteria.where("key").is("tickers.path")), Config.class);
        assert configTickers != null;
        String tickers = configTickers.getValue();
        procList.stream().filter(p->!ObjectUtils.isEmpty(p.getPid())).filter(p-> p.getShell().contains(tickers)).findFirst().ifPresent(proc -> {
            exec(proc::setMessage, "kill " + proc.getPid());
            proc.setUpdatetime(LocalDateTime.now());
            proc.setStatus("kill");
        });
        procList.stream().filter(p->ObjectUtils.isEmpty(p.getPid())).filter(p-> p.getShell().contains(tickers)).findFirst().ifPresent(ticker -> {
            exec(ticker::setMessage, ticker.getShell());
        });
        ps_ef(new Proc("ps -ef"), false);
        System.out.println("tickers重启完成");
    }

    public ProcessService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.threadPool = Executors.newFixedThreadPool(8);
    }

    public void addProc(Proc proc) {
        if (!procList.contains(proc)) procList.add(proc);
    }

    @Override
    public Reply insert(Class<?> clazz, API api, Object object) {
        Proc proc = (Proc) object;
        addProc(proc);
        return Reply.success(proc.getSn());
    }

    @Override
    public Reply delete(Class<?> clazz, API api, String sn) {
        procList.stream().filter(proc -> proc.getSn().toHexString().equals(sn)).findFirst().ifPresent(proc -> {
            if ("kill".equals(proc.getStatus())) {
                procList.remove(proc);
                return;
            }
            String pid = proc.getPid();
            if (!ObjectUtils.isEmpty(pid)) {
                exec(proc::setMessage, "kill " + pid);
            }
            proc.setStatus("kill");
        });
        return Reply.success();
    }

    @Override
    public Reply page(Class<?> clazz, API api, PageDto dto) {
        if (!procList.isEmpty() && procList.stream().anyMatch(proc -> proc.getShell().contains("ps -ef"))) {
            return Reply.success(procList).total(procList.size());
        }
        Proc proc = new Proc("ps -ef");
        addProc(proc);
        ps_ef(proc, true);
        Config configJava = mongoTemplate.findOne(new Query(Criteria.where("key").is("java.path")), Config.class);
        Config configJar = mongoTemplate.findOne(new Query(Criteria.where("key").is("jar.path")), Config.class);
        Config configPython = mongoTemplate.findOne(new Query(Criteria.where("key").is("python.path")), Config.class);
        Config configTickers = mongoTemplate.findOne(new Query(Criteria.where("key").is("tickers.path")), Config.class);
        Config configOperator = mongoTemplate.findOne(new Query(Criteria.where("key").is("operator.path")), Config.class);
        Config configClient = mongoTemplate.findOne(new Query(Criteria.where("key").is("client.path")), Config.class);
        if (!ObjectUtils.isEmpty(configPython)
                && !ObjectUtils.isEmpty(configOperator)
                && !ObjectUtils.isEmpty(configClient)
                && !ObjectUtils.isEmpty(configTickers)) {
            System.out.println("开始组装命令");
            String python = configPython.getValue();
            String tickers = configTickers.getValue();
            String operator = configOperator.getValue();
            String client = configClient.getValue();
            addProc(new Proc(String.join(" ", "nohup", configJava.getValue(), "-jar", configJar.getValue(), "> strategy.log 2>&1", "&")));
            List<Operator> operators = mongoTemplate.find(new Query(Criteria.where("deleted").is(0)), Operator.class);
            List<Account> accounts = mongoTemplate.find(new Query(Criteria.where("deleted").is(0)), Account.class);

            Proc ticker = new Proc(String.join(" ", "nohup", python, tickers, ">tickers.log 2>&1", "&"));
            exec(ticker::setMessage, ticker.getShell());
            addProc(ticker);

            Proc start = new Proc(String.join(" ", "nohup", python, operator, ">account.log 2>&1", "&"));
            exec(start::setMessage, start.getShell());
            addProc(start);
            accounts.forEach(ac -> {
                operators.stream()
                        .filter(op -> op.account.equals(ac.account))
                        .findFirst()
                        .ifPresent(op -> {
                            String shell = String.join(" ", "nohup", python, client, ac.apikey, ac.secretkey, ac.passphrase, op.account, "> clients.log 2>&1", "&");
                            Proc begin = new Proc(shell);
                            exec(begin::setMessage, shell);
                            addProc(begin);
                        });
            });
        }
        return Reply.success(procList).total(procList.size());
    }

    private void ps_ef(Proc proc, boolean restart) {
        if (restart) {
            System.out.println("重启python3.11进程");
            procList.stream()
                    .filter(p -> !ObjectUtils.isEmpty(p.getPid()))
                    .filter(p -> !"kill".equals(p.getStatus()))
                    .filter(p -> p.getShell().contains("python3.11"))
                    .forEach(p -> {
                        exec(p::setMessage, "kill " + p.getPid());
                        p.setStatus("kill");
                    });
        }
        procList.removeIf(ps -> ps.getShell().startsWith("root"));
        exec(line -> {
            if (line.contains("java") || line.contains("python")) {
                String[] split = line.split("\\s+");
                addProc(new Proc(split[1], line));
            }
        }, proc.getShell());
    }

    private void exec(Consumer<String> consumer, String shell) {
        threadPool.submit(() -> {
            try {
                System.out.println(shell);
                Runtime runtime = Runtime.getRuntime();
                Process p = runtime.exec(shell);
                BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(p.getInputStream())));
                String line;
                while ((line = reader.readLine()) != null) {
                    consumer.accept(line);
                }
            } catch (Exception e) {
                consumer.accept(e.getMessage());
            }
        });
    }

    @Override
    public Reply update(Class<?> clazz, API api, UpdateDto dto) {
        procList.stream().filter(proc -> proc.getSn().toHexString().equals(dto.getSn())).findFirst().ifPresent(proc -> {
            String head = dto.getTitle();
            Z.set(head, proc, dto.getNewVal());
            proc.setUpdatetime(LocalDateTime.now());
            if ("status".equals(head)) {
                // 修改状态：要么kill要么运行
                if ("kill".equals(dto.getNewVal()) && !ObjectUtils.isEmpty(proc.getPid())) {
                    exec(proc::setMessage, "kill " + proc.getPid());
                } else if ("running".equals(dto.getNewVal()) && ObjectUtils.isEmpty(proc.getPid())) {
                    if ("ps -ef".equals(proc.getShell())) {
                        ps_ef(proc, false);
                    } else {
                        exec(proc::setMessage, proc.getShell());
                    }
                }
            }
        });
        return Reply.success(dto.getSn());
    }
}
