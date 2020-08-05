package com.learning.concurrency.forkjoin;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.DoublePredicate;

/***
 * Java SE 7 中新引入了 Fork-Join 框架，专门用来支持可能对每个处理器内核分别使用一个线程，来完成计算密集型任务（如图像或视频处理）的
 * 一类应用。他可以将处理任务很自然地分解为子任务，如下所示：
 *  if(problem < threshoid)
 *      solve the problem
 *  else{
 *      break the problem into subproblems
 *      recursively solve each subproblem
 *      combine the results
 *  }
 */
public class ForkandJoinTest {

    public static void main(String[] args) {

        final int SIZE = 100;
        double[] data = new double[SIZE];
        for(int i = 0; i < data.length; i ++) {
            data[i] = Math.random();
            System.out.println("第" + i + "个值：" + data[i] );
        }

        ForkJoinPool pool = new ForkJoinPool();
        Countor countor = new Countor(data, x -> x > 0.5, 0, SIZE);
        // 这个 invoke() 是 ForkJoinPool 的方法
        pool.invoke(countor);
        System.out.println("达标量：" + countor.join());
    }

}
class Countor extends RecursiveTask<Integer>{
    
    private final int THRESHOLD = 10;
    private final int from;
    private final int to;
    private final double[] data;
    private final DoublePredicate filter;
    
    public Countor(double[] data, DoublePredicate filter, int from, int to){
        this.data = data;
        this.filter = filter;
        this.from = from;
        this.to = to;
    }
    
    public Integer compute(){
        System.out.println("I have starting");
        //int count = 0;
        if(to - from < THRESHOLD){
            int count = 0;
            for(int i = from; i < to; i++){
                if(filter.test(data[i])) {
                    count++;
                    System.out.println(data[i] + "达标!");
                }
                else{
                    System.out.println("第" + i + "个不达标，值：" + data[i]);
                }
            }
            return count;
        }else{
            int mid = (to + from) / 2;
            Countor first = new Countor(data, filter, from, mid);
            Countor second = new Countor(data, filter, mid, to);
            // 这个 invokeAll() 是继承自 ForkJoinTask 的方法
            invokeAll(first,second);
            return first.join() + second.join();
        }
        //return count;
    }
}
