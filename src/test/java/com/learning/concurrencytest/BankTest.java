package com.learning.concurrencytest;

//import com.learning.concurrency.unsynch.Bank;
import com.learning.concurrency.lockandcondition.Bank;
//import com.learning.concurrency.mysynchronized.Bank;

public class BankTest {

    /**
     * 前三个实例域的值将决定该程序是否会出现死锁
     * 例如：
     * 100、1000、1000 --> 不会死锁
     * 10、1000、2000 --> 会死锁
     */
    private static final int NACCOUNTS = 10;
    private static final double INITIAL_BALANCE = 1000;
    private static final double MAX_AMOUNT = 2000;
    private static final int DELAY = 10;

    public static void main(String[] args){
        final Bank bank = new Bank(NACCOUNTS, INITIAL_BALANCE);
        // 从 bank.accounts 中的第一个账户开始，依次转出 0 至 MAX_AMOUNT 中的任意数额存款到任意账户
        for(int i = 0; i < NACCOUNTS; i ++){
            final int formAccount = i;

            // !!! 原来一个类还真能被塞到一个for循环的某个代码块内，还真是“能屈能伸”
            class Work implements Runnable{
                @Override
                public void run(){
                    // 处理 Thread.sleep() 可能抛出的异常
                    try{
                        // 每个线程都会一直重复从该账号转出的操作
                        while(true){
                            int toAccount = (int) (NACCOUNTS * Math.random());
                            double amount = MAX_AMOUNT * Math.random();
                            // ??? 要是不用 lambda 表达式，还真不知道 bank、formAccount 该怎么传递
                            bank.transfer(formAccount, toAccount, amount);
                            Thread.sleep((int) (DELAY * Math.random()));
                        }
                    }catch (InterruptedException e){
                        //e.printStackTrace();
                    }
                }
            }
            /*
            // 使用 lambda 表达式处理“函数式接口” Runnable
            Runnable r = () -> {
                // 处理 Thread.sleep() 可能抛出的异常
                try{
                    // 每个线程都会一直重复从该账号转出的操作
                    while(true){
                        int toAccount = (int) (NACCOUNTS * Math.random());
                        double amount = MAX_AMOUNT * Math.random();
                        // ??? 要是不用 lambda 表达式，还真不知道 bank、formAccount 该怎么传递
                        bank.transfer(formAccount, toAccount, amount);
                        Thread.sleep((int) (DELAY * Math.random()));
                    }
                }catch (InterruptedException e){
                    //e.printStackTrace();
                }
            };
            */
            Thread t = new Thread(new Work());
            t.start();
        }
    }

}
