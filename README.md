# Multithreaded applications - gas station

## Постановка задачи
> В данной работе необходимо написать многопоточное приложение, которое эмулирует заданную модель. Студент сам
> должен спроектировать потоки, которые отвечают за поведение сущностей из полученного задания. Взаимодействие
> потоков должно быть синхронизировано и приложение должно быть протестирование на наличие dead locks и race
> conditions. Приложение не должно переставать работать из-за изменения задержек и модель не должна быть полностью
> синхронной.
>
> _Заправочная станция_.
> 
> Заправочная станция с самообслуживанием имеет некоторое число насосов для заправки
> автомобилей покупателей топливом.
> Действует следующая система:
> - покупатели сначала платят кассиру за топливо;
> - кассир активирует насос чтобы доставить топливо.
>
> Возьмите систему с несколькими покупателями и заправочной станцией с тремя насосами а одним кассиром.
> Учитывая условие, что плата за топливо может быть различной,
> необходимо удостоверится что не будет неудовлетворенного покупателя (если насос выдал неправильный объем
> топлива).
## Реализация программы 
Программа реализована посредством нескольких классов:
1. _Buyer_ и _BuyersThread_- представляет покупателя на заправочной станции и поток клиентов на заправочной станции.
   
   Первый класс предоставляет функциональность для создания покупателя, выбора типа топлива,
   создания заказа и оплаты заказа. представляет поток клиента на заправочной станции и моделирует его поведение.
   
   Второй класс позволяет моделировать клиентов на заправочной станции и синхронизировать их действия с использованием семафоров и блокирующих очередей.
3. _Cashier_ и _CashierThread_ - представляет кассира и его поток на заправочной станции.
   
   _Cashier_ играет ключевую роль в обслуживании клиентов на заправочной станции, принимая заказы и направляя их на соответствующие насосы для заправки.
   
   Второй класс координирует работу кассира на заправочной станции, принимает заказы от покупателей и передает их на соответствующие топливные насосы для выполнения.
4. _FOrder_ - представляет заказ на заправочной станции.
  
   Этот класс позволяет создавать, хранить и управлять информацией о заказах на заправочной станции,
   включая их тип топлива и сумму.
5. _FPump_ и _FPumpThread_ - представляет топливный насос и его поток на заправочной станции.
   
   Первый класс имитирует работу топливного насоса, включая случайные факторы, такие как возможные ошибки в выдаче топлива, и предоставляет методы для проверки
   правильности выполнения заказа.

   Этот класс позволяет создать поток, который моделирует работу заправочного насоса, выполняя заказы на заправку и управляя доступом к насосу с использованием
   семафора.
7. _FType_ - представляет перечисление, которое определяет различные виды топлива на станции.

   Этот класс упрощает работу с разными видами топлива на станции, предоставляя доступ к их цене и названию через методы.

В классе _Main_ cоздается семафор, который представляет доступ к трём заправочным насосам, блокирующие очереди (очередь из 3-х клиентов на кассу и работа кассира), 
обычная очередь для хранения топливных насоов. Так же реализован массив семафоров, представляющих процесс заправки на каждом насосе.

В цикле for создаются и запускаются три потока, представляющих работу топливных насосов,  где каждый связан с соответствующим семафором из
массива, что позволяет контролировать доступ к каждому насосу. Создается и запускается поток, предоставляющий
управление очередью клиентов и обслуживание их.

Во втором цикле for создаются и запускаются 10 потоков, представляющих клиентов, которые будут подходить к заправочным насосам и занимать место в 
очереди кассы. 
## Достоинства и недостатки реализации.
_Достоинства_:
- Использование семафоров позволяет управлять доступом к ограниченным ресурсам, таким как топливные насосы, обеспечивая правильное распределение и контроль доступа к ним.
- Блокирующие очереди обеспечивают удобное управление потоками, синхронизацию и безопасное общение между различными частями программы.
- Программа моделирует реальную работу заправочной станции с клиентами, кассиром и топливными насосами, что позволяет проверить и оптимизировать процессы.

_Недостатки_:
- Понимание и отладка программы с многопоточностью может быть сложной задачей из-за возможных гонок и асинхронного выполнения.
- Программа моделирует упрощенную версию работы заправочной станции и не учитывает всех возможных сценариев и деталей реального мира.
  Реальные заправочные станции могут иметь более сложные бизнес-процессы и взаимодействие с клиентами.

## Результат работы приложения
Покупатель воспользовался топливным насосом. 

<p align="center">
<picture>
   <img src="https://github.com/boevAV/JavaLabs/blob/c19449d69ff973560980d98f08bb4b0a1ffbef51/has%20taken%20over%20the%20fuel%20pump.png" width="350">
</picture>
</p>

Покупатель оплатил топливо (с указанием вида топлива, цены).

<p align="center">
<picture>
   <img src="https://github.com/boevAV/JavaLabs/blob/c19449d69ff973560980d98f08bb4b0a1ffbef51/has%20paid%20for%20fuel.png" width="350">
</picture>
</p>

Кассир получил новый заказ и активировал насос.

Насос начал работу и выдал отчёт о корректности выполненной работы.

<p align="center">
<picture>
   <img src="https://github.com/boevAV/JavaLabs/blob/c19449d69ff973560980d98f08bb4b0a1ffbef51/finish.png" width="350">
</picture>
</p>
