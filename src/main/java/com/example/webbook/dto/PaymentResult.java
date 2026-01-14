package com.example.webbook.dto;

public class PaymentResult {
    private String orderId;
    private String status;
    private boolean success;
    private String payerName;
    private String payerEmail;
    private double amount;
    private String currency;

    public PaymentResult() {
    }

    public PaymentResult(String orderId, String status, boolean success, String payerName, String payerEmail, double amount, String currency) {
        this.orderId = orderId;
        this.status = status;
        this.success = success;
        this.payerName = payerName;
        this.payerEmail = payerEmail;
        this.amount = amount;
        this.currency = currency;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
