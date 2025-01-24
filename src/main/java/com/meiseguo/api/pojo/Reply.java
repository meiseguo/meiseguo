package com.meiseguo.api.pojo;

public class Reply {
    int state;
    String msg;
    Object data;
    long total = 0;
    public Reply(){}
    public Reply(Object data){
        this.state = 200;
        this.data = data;
        this.msg = "success";
    }
    public Reply(int state, String msg) {
        this.state = state;
        this.msg   = msg;
    }
    static Reply FAIL = new Reply(-1, "fail");
    static Reply SUCCESS = new Reply(200, "");
    static Reply FORBID = new Reply(403, "403 forbid");
    public static Reply fail(){
        return FAIL;
    }
    public static Reply fail(String reason){
        return new Reply(-1, reason);
    }
    public static Reply success(){
        return SUCCESS;
    }
    public static Reply success(Object data){
        Reply reply = new Reply(200, "success");
        reply.setData(data);
        return reply;
    }

    public static Reply success(Object data, String msg){
        Reply reply = new Reply(200, msg);
        reply.setData(data);
        return reply;
    }
    public static Reply of(int state, String msg, Object data){
        Reply reply = new Reply(state, msg);
        reply.setData(data);
        return reply;
    }

    public static Reply forbid() {
        return FORBID;
    }
    public static Reply ok(String msg) {
        return new Reply(200, msg);
    }

    public Reply total(long total) {
        this.total = total;
        return this;
    }
    public boolean isSuccess() {
        return state == 200;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Reply{" +
                "state=" + state +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
