package com.meiseguo.api.pojo;

public enum StrategyType {
    buy("做多") {
        @Override
        public double open(double amount, double price) {
            return amount;
        }

        @Override
        public double close(Action open, double price) {
            return open.value/price;
        }
    }, sell("做空") {
        @Override
        public double open(double amount, double price) {
            return amount;
        }

        @Override
        public double close(Action open, double price) {
            return open.amount;
        }
    };
    public abstract double open(double amount, double price);
    public abstract double close(Action open, double price);
    StrategyType(String desc){}
}
