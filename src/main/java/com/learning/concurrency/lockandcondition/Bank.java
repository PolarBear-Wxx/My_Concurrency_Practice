package com.learning.concurrency.lockandcondition;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 一个使用 Lock 和 Condition 对象管理同步的 Bank 类
 */
public class Bank {

    // final 修饰的数组，指定数组指向的内存空间固定，但是数组的内部数值还是可以更改的
    // final 的初始化可以放在构造器中
    private final double[] accounts;
    private Lock bankLock;
    private Condition sufficientFunds;

    public Bank(int n, double initialBalance){
        accounts = new double[n];
        Arrays.fill(accounts, initialBalance);
        bankLock = new ReentrantLock();
        // Condition 对象的实例化要依附于对应的 Lock 对象
        sufficientFunds = bankLock.newCondition();
    }

    public void transfer(int form, int to, double amount){
        bankLock.lock();
        // 这里用 try 的目的就是为了后面的 finally
        try{
            while (accounts[form] < amount)
                sufficientFunds.await();
            System.out.print(Thread.currentThread());
            accounts[form] -= amount;
            accounts[to] += amount;
            System.out.print(" " + amount + " from " + form + " to " + to + " ##");
            // 不设置 condition 时，下面这行代码的输出会怎样？
            System.out.print(" accounts[" + form + "]" + " 还剩：" + accounts[form] + " ##");
            System.out.println(" TotalBalance: " + getTotalBalance());
            sufficientFunds.signalAll();
        }catch (InterruptedException e){ }
        finally {
            bankLock.unlock();
        }
    }

    public double getTotalBalance() {
        bankLock.lock();
        try{
        double sum = 0;
        for(double balance : accounts){
            sum += balance;
        }
        return sum;
        }
        finally {
            bankLock.unlock();
        }
    }
}
