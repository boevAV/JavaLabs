import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

public class CashierThread implements Runnable {

    private Cashier cashier;//Класс реализующий функции кассира

    private BlockingQueue<FOrder> queueCheckout;//Блокирующая очередь = моделирующая очередь в кассу

    private BlockingQueue<Semaphore> cashierJob;//Блокирующая очередь = моделирующая работу кассира

    private Queue<FPumpThread> fuelPumps;//Очередь потоков топливных насосов

    private FOrder order;//Класс, моделирующая заказ

    //Конструктор класса
    public CashierThread(BlockingQueue<FOrder> qCheckout, BlockingQueue<Semaphore> cJob, Queue<FPumpThread> fPumps) {
        cashier = new Cashier();
        queueCheckout = qCheckout;
        cashierJob = cJob;
        fuelPumps = fPumps;
    }

    @Override
    public void run() {//Метод, который моделирует работу кассира на заправочной станции, который
        // принимает заказы от покупателей, передает их на топливный насос для выполнения, а затем
        // информирует покупателей о начале заправки.
        while (!Thread.currentThread().isInterrupted()) {//бесконечный цикл, который выполняется до тех
            // пор, пока текущий поток кассира не будет прерван.
            //Кассир ожидает получение заказа от покупателя из очереди queueCheckout. Если заказ
            // доступен, он извлекается из очереди.
            try {
                order = queueCheckout.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                order = null;
            }
            //Выводится сообщение о том, что кассир получил новый заказ и указывается тип топлива и
            // сумма заказа
            System.out.println("Cashier has received a new order: Fuel "
                    + order.getFType().getName()
                    + "; Sum: " + order.getSum());
            //Кассир "засыпает" на 1 секунду (1000 миллисекунд)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Кассир отправляет заказ на топливный насос, указывая тип топлива и сумму заказа
            System.out.println("Cashier has sent a new order to the fuel pump: Fuel " + order.getFType().getName() + "; Sum " + order.getSum());
            FPumpThread pumpThread = fuelPumps.poll();//Получается доступ к свободному топливному насосу
            // с помощью объекта FPumpThread из очереди fuelPumps.
            FPump pump = pumpThread.getFuelPump();
            cashier.serveBuyer(pump, order);//Вызывается метод serveBuyer(pump, order), чтобы обслужить
            // покупателя на этом насосе.
            pumpThread.getPumpSemaphore().release();
            fuelPumps.add(pumpThread);//После обслуживания клиента, семафор насоса освобождается с
            // помощью pumpThread.getPumpSemaphore().release() (57 строка), и насос добавляется обратно
            // в очередь fuelPumps.
            //Кассир "засыпает" на еще 1 секунду (1000 миллисекунд).
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Кассир сообщает покупателю, когда топливо будет заправлено в автомобиль, помещая семафор
            // насоса в очередь cashierJob. Это сигнализирует покупателю о начале процесса заправки.
            try {
                cashierJob.put(pumpThread.getPumpSemaphore());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
