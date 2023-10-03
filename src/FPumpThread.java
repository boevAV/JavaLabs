import java.util.concurrent.Semaphore;

public class FPumpThread implements Runnable {
    private FPump fPump;//Класс, моделирующий заправочный насос

    private Semaphore pSemaphore;//Семафор, по которому заправочный насос сообщает о выполнении работы

    public FPumpThread(Semaphore pumpSemaphore) {//Конструктор класса
        fPump = new FPump();
        pSemaphore = pumpSemaphore;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {//бесконечный цикл, который выполняется до тех
            // пор, пока текущий поток не будет прерван (isInterrupted() проверяет, был ли поток прерван).
            try {
                pSemaphore.acquire();//ожидает доступ к ресурсу, представленному семафором pSemaphore.
                // Если семафор недоступен (другой поток уже его занял), текущий поток будет блокирован и
                // ожидать, пока семафор не станет доступным.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(getPumpName() + " has started");//Выводит информацию о том, что топливный
            // насос начал работу.
            int fVolume;//Переменная fVolume, которая будет содержать объем топлива.
            do {//Выполняет заказ на заправку и проверяет правильность выполнения заказа. Он будет
                // выполняться, пока заказ не будет выполнен правильно (пока checkFuelVolume(fuelVolume)
                // возвращает false).
                //Внутри этого блока код сообщает о том, было ли заправлено правильное количество
                // топлива или нет, и выводит соответствующее сообщение.
                fVolume = fPump.giveFuel();
                if (fPump.checkFuelVolume(fVolume)) {
                    System.out.println(getPumpName() + " has poured CORRECT amount of fuel: Fuel "
                            + fPump.getFuelType().getName()
                            + "; Sum " + fPump.getSum()
                            + "; Vol " + fVolume);
                }
                else {
                    System.out.println(getPumpName() + " has poured INCORREC amount of fuel: Fuel "
                            + fPump.getFuelType().getName()
                            + "; Sum " + fPump.getSum()
                            + " Top up to correct amount.");
                }

            } while (!fPump.checkFuelVolume(fVolume));
            try {
                pSemaphore.release();//После завершения заказа, топливный насос освобождает семафор,
                // позволяя другим насосам начать работу. Затем поток спит 5 секунды, имитируя период
                // между заказами.
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public FPump getFuelPump() {//Метод, возвращающий объект класса заправочного насоса
        return fPump;
    }

    public Semaphore getPumpSemaphore() {//Метод, возвращающий семафор, по которому заправочный
        // насос сообщает о выполненной работе
        return pSemaphore;
    }

    private String getPumpName() {//Метод, возвращающий название заправочного насоса
        String name = "Pump" + Thread.currentThread().getName().substring(6);
        return name;
    }
}