import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

public class Main {
    public static void main( String[] args ) {
        Semaphore fillingPumps = new Semaphore(3, true);//Счётный семафор = контроллирующий доступ к
        // заправочным насосам
        BlockingQueue<FOrder> queueCheckout = new ArrayBlockingQueue<>(3, true);//Блокирующая очередь =
        // моделирующая очередь в кассу
        BlockingQueue<Semaphore> cashierJob = new ArrayBlockingQueue<>(1, true);//Блокирующая очередь =
        // моделирующая работу кассира
        Queue<FPumpThread> fuelPumps = new LinkedList<>();//Очередь потоков топливных насосов
        Semaphore refuelingProcess[] = {new Semaphore(1), new Semaphore(1), new Semaphore(1)};
        //Семафоры, моделирующие процесс заправки

        for (int i = 0; i < 3; i++) {//создает и запускает три потока, представляющих топливные насосы
            try {
                refuelingProcess[i].acquire();//ожидает, пока блокирующий ресурс refuelingProcess[i] станет доступным.
                // Если ресурс недоступен, текущий поток будет ожидать, пока ресурс не станет доступным.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            FPumpThread fuelPumpThread = new FPumpThread(refuelingProcess[i]);//Создается объект FuelPumpThread,
            // который является потоком для одного из топливных насосов, и передается ресурс refuelingProcess[i].
            fuelPumps.add(fuelPumpThread);
            new Thread(fuelPumpThread).start();//Создается новый поток, связанный с объектом fuelPumpThread, и этот
            // поток запускается с помощью метода start(). Как результат, каждый из трех топливных насосов будет
            // выполняться в собственном потоке параллельно.
        }

        new Thread(new CashierThread(queueCheckout, cashierJob, fuelPumps)).start();//поток кассира

        for (int i = 0; i < 10; i++) {//поток клиента
            new Thread(new BuyersThread(fillingPumps, queueCheckout, cashierJob)).start();
        }
    }
}
