package com.learning.concurrency.unsynch;

import java.util.Arrays;

public class Bank {

    private final double[] accounts;

    public Bank(int n, double initialBalance){
        accounts = new double[n];
        Arrays.fill(accounts, initialBalance);
    }

    public void transfer(int form, int to, double amount){
        if(accounts[form] < amount) return;
        System.out.print(Thread.currentThread());
        accounts[form] -= amount;
        accounts[to] += amount;
        System.out.print(" " + amount + " from " + form + " to " + to);
        System.out.println(" Total Balance: " + getTotalBalance());
    }

    public double getTotalBalance() {
        double sum = 0;
        for(double balance : accounts){
            sum += balance;
        }
        return sum;
    }
}
