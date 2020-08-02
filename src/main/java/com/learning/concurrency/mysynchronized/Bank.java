package com.learning.concurrency.mysynchronized;

import java.util.Arrays;

public class Bank {

    private final double[] accounts;

    public Bank(int n, double initialBalance){
        accounts = new double[n];
        Arrays.fill(accounts, initialBalance);
    }

    public synchronized void transfer(int form, int to, double amount) throws InterruptedException{
        //while (accounts[form] < amount)
        //    wait();
        System.out.print(Thread.currentThread());
        accounts[form] -= amount;
        accounts[to] += amount;
        System.out.print(" " + amount + " from " + form + " to " + to + " ##");
        // 不设置 condition 时，下面这行代码的输出会怎样？
        System.out.print(" accounts[" + form + "]" + " 还剩：" + accounts[form] + " ##");
        System.out.println(" TotalBalance: " + getTotalBalance());
        //notifyAll();
    }

    public double getTotalBalance() {
        double sum = 0;
        for(double balance : accounts){
            sum += balance;
        }
        return sum;
    }
}
