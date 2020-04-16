/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package threadlocktask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Юрий
 */
public class ThreadLockTask
{
    // 1203 - без синхронизации
    // 15033 - с полной синхронизацией
    // 5826 - с частичной синхронизацией
    public static long startTime;
    public static void main(String[] args) throws Exception {
        // Общий ресурс
        Data d = new Data();
        ExecutorService es = Executors.newFixedThreadPool(15);
        startTime = System.currentTimeMillis();
        // Запускаем 50 задач в пул
        for(int i=0; i<50; i++)
            es.submit(new WorkWData(d));

        // TimeUnit.SECONDS.sleep(3);
        // Запрещаем добавлять новые задачи в пул
        es.shutdown();
        // es.submit(new WorkWData(d));
        //es.shutdownNow();
        // Блокирует текущий поток выполнения, пока все задачи не будут выполнены
        // после вызова shutdown()
        // es.awaitTermination(5000, TimeUnit.MILLISECONDS);
        while (!es.isTerminated()){
            Thread.sleep(10);
        }
        //es.shutdownNow();
        System.out.println(System.currentTimeMillis() - ThreadLockTask.startTime);
        System.out.println("The End");
    }
}

// Класс, описывающий задачу
class WorkWData implements Runnable {
    Data obj;
    WorkWData(Data d) {
        obj=d;
     }
    public void run() {
        int n;
        n = obj.read();
        System.out.println("First read"+" "+Thread.currentThread().getName()+" "+new Integer(n).toString());
        obj.write();
        n=obj.read();
        System.out.println("Second read"+" "+Thread.currentThread().getName()+" "+new Integer(n).toString());
        /* if (n == 51) {
            System.out.println(System.currentTimeMillis() - ThreadLockTask.startTime);
        } */
    }
}

// Общий (разделяемый объект)
class Data {

    int count = 1;
    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    // Замок, вызвав который можно получить разделяемый ресурс для:
    // считывания значений текущим потоком,
    // считывания любыми другими потоками,
    // запрета записи всеми потоками, включая текущий
    Lock readLock = readWriteLock.readLock();
    // Замок, вызвав который можно получить разделяемый ресурс для:
    // чтения и записи текущим потоком,
    // запрета чтения и записи всеми остальными потоками
    Lock writeLock = readWriteLock.writeLock();

    int read(){
        try {
            writeLock.lock();
            readLock.lock();
            int n = count;
            TimeUnit.MILLISECONDS.sleep(100);
            count = n;
        } catch (InterruptedException ex) { }
        finally
        {
            writeLock.unlock();
            readLock.unlock();
        }
        return count;
    }
    void write(){
        try {
            writeLock.lock();
            readLock.lock();
            int n = count;
            TimeUnit.MILLISECONDS.sleep(100);
            n++;
            count=n;
        } catch (InterruptedException ex) { }
        finally
        {
            writeLock.unlock();
            readLock.unlock();
        }
    }
    
}
