import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;

class CalcVector {
    BigDecimal[] elVector;//Элементы вектора
    static int THREADS = 4;//Количества потоков

    CalcVector(BigDecimal[] arr) {//Конструктор класса
        elVector = Arrays.copyOf(arr, arr.length);
    }

    CalcVector(int size) {//Конструктор класса
        elVector = new BigDecimal[size];
    }

    public CalcVector(CalcVector tVector) {//Конструктор класса, создающий глубокую копию вектора
        elVector = Arrays.copyOf(tVector.getElVector(), tVector.sizeVector());
    }

    public CalcVector(CalcVector vector, CalcVector other) {//Конструктор класса, создающий
        // комбинированный вектор
        elVector = Arrays.copyOf(vector.getElVector(), vector.sizeVector() + other.sizeVector());
        System.arraycopy(other.getElVector(), 0, elVector, vector.sizeVector(), other.sizeVector());
    }

    public CalcVector(BigDecimal x) {//Конструктор класса
        elVector = new BigDecimal[1];
        elVector[0] = x;
    }

    int sizeVector() {//Метод, возвращающий размер вектора
        return elVector.length;
    }

    BigDecimal[] getElVector() {//Метод, возвращающий массив элементов вектора
        return elVector;
    }

    void setС(BigDecimal elem, int coordinates) {//Метод для установки значений элементов вектора по
        // указанным координатам
        elVector[coordinates] = elem;
    }

    BigDecimal getС(int coordinates) {//Метод для получения значений элементов вектора по указанным
        // координатам
        return elVector[coordinates];
    }

    CalcVector addVectors(CalcVector v) throws IndexOutOfBoundsException {//Метод, выполняющий сложение
        // векторов
        return applyBinaryFuToElVectors(v, BigDecimal::add);
    }

    CalcVector addVectorNumber(BigDecimal val) {//Метод, складывающий вектор и число
        CalcVector nVector = new CalcVector(sizeVector());//Вектор, хранящий результат сложения
        for (int i = 0; i < sizeVector(); i++)
            nVector.setС(elVector[i].add(val), i);//Перебирает элементы текущего вектора и добавляет
        // val к каждому элементу, используя метод setС
        return nVector;
    }

    CalcVector multiply(BigDecimal val) {//Метод, умножающий каждый элемент вектора на число val
        CalcVector nVector = new CalcVector(sizeVector());//Вектор, который будет содержать результат
        // умножения
        for (int i = 0; i < sizeVector(); i++)
            //Каждый элемент вектора умножается на значение val с использованием метода multiply
            // класса BigDecimal. Результат этой операции представляет собой новый BigDecimal, который
            // будет содержать умноженное значение
            nVector.setС(elVector[i].multiply(val), i);// Полученное умноженное значение устанавливается в
            // новом объекте nVector на ту же позицию, где находится элемент в исходном векторе
        return nVector;
    }

    CalcVector applyFunction(Function<BigDecimal, BigDecimal> function) {
        CalcVector nVector = new CalcVector(sizeVector());//Вектор, который будет содержать результат
        // применения функции
        for (int i = 0; i < sizeVector(); i++)
            nVector.setС(function.apply(elVector[i]), i);//Функция применяется к текущему элементу
        //Результат применения функции устанавливается в новом объекте nVector на ту же позицию, где
        // находится элемент в исходном векторе.
        return nVector;
    }

    static CalcVector addVectors(CalcVector firstV, CalcVector secondV)
            throws ExecutionException, InterruptedException {//Метод предоставляет удобный способ
        // сложения двух векторов
        return applyFunction(firstV, secondV, BigDecimal::add);//Параллельно применяется функция
        // (в данном случае BigDecimal::add) к соответствующим элементам векторов firstV и secondV.
        // Он вернет новый вектор, содержащий результаты операции сложения.
    }

    static CalcVector addVectors(CalcVector... vs) {//Метод позволяет сложить несколько векторов и
        // вернуть их сумму в виде нового объекта
        CalcVector nVector = new CalcVector(vs[0]);//Этот объект будет использоваться для хранения
        // результата сложения всех переданных векторов
        // В каждой итерации цикла выполняется метод addVectors объекта nVector с переданным вектором
        // Это означает, что текущий nVector увеличивается путем сложения его значений с значениями из
        // вектора vs[i].
        for (int i = 1; i < vs.length; i++)
            nVector = nVector.addVectors(vs[i]);
        return nVector;
    }

    static CalcVector addVectorNumber(CalcVector v, BigDecimal val)//Метод позволяет параллельно сложить
        // каждый элемент вектора v с значением val и возвращать новый вектор nVector с результатами
            throws ExecutionException, InterruptedException {
        CalcVector nVector = new CalcVector(v.sizeVector());//Новый объект, который будет содержать
                // результаты сложения каждого элемента вектора v с значением val
        ExecutorService exec = Executors.newFixedThreadPool(THREADS);//Пул исполнителей будет
                // использоваться для параллельного выполнения операции сложения элементов вектора с
                // значением val.
        Future<BigDecimal>[] futures = new Future[v.sizeVector()];//Создается массив объектов
                // Future<BigDecimal>, который будет содержать будущие результаты выполнения сложения.
                // В этом цикле каждый элемент вектора складывается с значением val с использованием
                // метода add(), и эта операция сложения выполняется параллельно в отдельном потоке.
                // Результаты сложения помещаются в соответствующие элементы массива futures.
                // Используется finalI, чтобы создать "захватывающую финальную переменную".
                for (int i = 0; i < v.sizeVector(); i++) {
            int finalI = i;
            futures[i] = exec.submit(() -> v.elVector[finalI].add(val));
        }
        exec.shutdown();//Пул исполнителей закрывается.
                //В этом цикле каждый результат из futures получается с помощью метода get() и
                // устанавливается в объекте nVector на соответствующей позиции i
        for (int i = 0; i < nVector.sizeVector(); i++) {
            nVector.setС(futures[i].get(), i);
        }
        return nVector;
    }

    static CalcVector multiply(CalcVector v, BigDecimal val)
            throws ExecutionException, InterruptedException {//Метод позволяет параллельно умножить
        // каждый элемент вектора v на значение val и возвращать новый вектор nVector с результатами
        CalcVector nVector = new CalcVector(v.sizeVector());//Новый объект, который будет содержать
        // результаты умножения каждого элемента вектора на значение val
        ExecutorService exec = Executors.newFixedThreadPool(THREADS);//Пул исполнителей будет
        // использоваться для параллельного выполнения умножения элементов вектора на значение val
        Future<BigDecimal>[] futures = new Future[v.sizeVector()];//Создается массив объектов
        // Future<BigDecimal>, который будет содержать будущие результаты выполнения умножения
        //В этом цикле каждый элемент вектора умножается на значение val с использованием метода
        // multiply(), и это умножение выполняется параллельно в отдельном потоке. Результаты умножения
        // помещаются в соответствующие элементы массива futures. Используется finalI, чтобы создать
        // "захватывающую финальную переменную".
        for (int i = 0; i < v.sizeVector(); i++) {
            int finalI = i;
            futures[i] = exec.submit(() -> v.elVector[finalI].multiply(val));
        }
        exec.shutdown();//Пул исполнителей закрывается
        //В этом цикле каждый результат из futures получается с помощью метода get() и устанавливается
        // в объекте nVector на соответствующей позиции i.
        for (int i = 0; i < nVector.sizeVector(); i++) {
            nVector.setС(futures[i].get(), i);
        }
        return nVector;
    }

    static CalcVector applyFunction(CalcVector firstV, CalcVector secondV, BiFunction<BigDecimal, BigDecimal, BigDecimal> function)
            throws ExecutionException, InterruptedException {//Метод позволяет параллельно выполнять
        // бинарную функцию над парами элементов из двух векторов и возвращать новый вектор nVector с
        // результатами
        //Эта строка проверяет, равны ли размеры векторов firstV и secondV. Если размеры не совпадают, то
        // выбрасывается исключение IndexOutOfBoundsException. Это гарантирует, что операция применения
        // функции может быть выполнена только между векторами одинаковой длины.
        if (firstV.sizeVector() != secondV.sizeVector())
            throw new IndexOutOfBoundsException();
       CalcVector nVector = new CalcVector(firstV.sizeVector());//Новый объект, который будет содержать
        // результаты применения бинарной функции function к соответствующим парам элементов из двух векторов.
        ExecutorService exec = Executors.newFixedThreadPool(THREADS);//Создается пул исполнителей
        // (ExecutorService) с фиксированным числом потоков (THREADS). Этот пул исполнителей будет
        // использоваться для параллельного выполнения бинарной функции над элементами векторов.
        Future<BigDecimal>[] futures = new Future[firstV.sizeVector()];//Создается массив объектов
        // Future<BigDecimal>, который будет содержать будущие результаты выполнения бинарной функции.
        //В этом цикле каждая пара элементов firstV.elVector[i] и secondV.elVector[i] передается в
        // функцию, которая выполняется параллельно в отдельном потоке. Результаты функций помещаются в
        // соответствующие элементы массива futures. Используется finalI, чтобы создать "захватывающую
        // финальную переменную".
        for (int i = 0; i < firstV.sizeVector(); i++) {
            int finalI = i;
            futures[i] = exec.submit(() -> function.apply(firstV.elVector[finalI], secondV.elVector[finalI]));
        }
        exec.shutdown();//Пул исполнителей закрывается
        //В этом цикле каждый результат из futures получается с помощью метода get() и устанавливается в
        // объекте nVector на соответствующей позиции i.
        for (int i = 0; i < nVector.sizeVector(); i++) {
            nVector.setС(futures[i].get(), i);
        }
        return nVector;
    }

    public static CalcVector applyFunctions(CalcVector v, Function<CalcVector, BigDecimal>[] arrFunctions)
            throws ExecutionException, InterruptedException {//метод позволяет параллельно выполнять
        // набор функций на входном векторе и возвращать новый вектор с результатами. Результаты функций
        // сохраняются в массиве futures, а затем извлекаются и устанавливаются в объекте nVector.
        CalcVector nVector = new CalcVector(arrFunctions.length);//Объект будет содержать результаты
        // применения каждой функции к входному вектору
        ExecutorService exec = Executors.newFixedThreadPool(THREADS);//Создается пул исполнителей
        // (ExecutorService) с фиксированным числом потоков (THREADS). Этот пул исполнителей будет
        // использоваться для параллельного выполнения функций
        Future<BigDecimal>[] futures = new Future[arrFunctions.length];//Создается массив объектов
        // Future<BigDecimal>, который будет содержать будущие результаты выполнения функций
        // В этом цикле каждая функция arrFunctions[i] выполняется параллельно в отдельном потоке, и
        // ее результат помещается в соответствующий элемент массива futures. Здесь используется
        // finalI, чтобы создать "захватывающую финальную переменную", чтобы функция могла иметь
        // доступ к правильному индексу i.
        for (int i = 0; i < arrFunctions.length; i++) {
            int finalI = i;
            futures[i] = exec.submit(() -> arrFunctions[finalI].apply(v));
        }
        exec.shutdown();//Пул исполнителей закрывается
        //В этом цикле каждый результат из futures получается с помощью метода get() и устанавливается
        // в объекте nVector на соответствующей позиции i.
        for (int i = 0; i < arrFunctions.length; i++) {
            nVector.setС(futures[i].get(), i);
        }
        return nVector;
    }

    static CalcVector mergeVectors(CalcVector firstV, CalcVector secondV) {//Вызывает метод
        // mergeVectors на объекте firstV и возвращает результат.
        // Передает вызов метода mergeVectors объекту firstV с объектом secondV в качестве аргумента и
        // возвращает результат выполнения этого метода. Это позволяет создать новый объект
        // CalcVector, который представляет объединение (слияние) двух векторов firstV и secondV.
        return firstV.mergeVectors(secondV);
    }

    CalcVector applyBinaryFuToElVectors(CalcVector v, BiFunction<BigDecimal, BigDecimal, BigDecimal> function) {
        // Cтрока проверяет, равны ли размеры текущего вектора и
        // вектора v. Если размеры не совпадают, то выбрасывается исключение
        // IndexOutOfBoundsException. Это гарантирует, что операция применения функции может быть
        // выполнена только между векторами одинаковой длины
        if (sizeVector() != v.sizeVector())
            throw new IndexOutOfBoundsException();
        CalcVector nVector = new CalcVector(sizeVector());//Этот новый объект будет содержать результаты
        // операции применения бинарной функции к соответствующим парам элементов из двух векторов
        for (int i = 0; i < sizeVector(); i++)
            //Бинарная функция применяется к элементам текущего вектора и вектора v, соответствующим
            // позиции i. Эта операция объединяет элементы из обоих векторов
            nVector.setС(function.apply(elVector[i], v.getС(i)), i);
        return nVector;
    }

    CalcVector applyArrFuToElVectors(Function<CalcVector, BigDecimal>[] arrFunctions) {//Метод
        // позволяет применять массив функций к каждому элементу объекта CalcVector и возвращать новый
        // вектор с результатами.
        CalcVector nVector = new CalcVector(arrFunctions.length);//Объект будет содержать результаты применения
        // каждой функции к текущему вектору
        for (int i = 0; i < arrFunctions.length; i++)
            //Здесь каждая функция применяется к текущему объекту CalcVector, используя метод apply
            // объекта типа Function<CalcVector, BigDecimal>. Каждая функция принимает CalcVector и
            // возвращает BigDecimal.
            nVector.setС(arrFunctions[i].apply(this), i);//Результат применения функции
            // устанавливается в новом объекте nVector на позицию i.
        return nVector;
    }

    CalcVector mergeVectors(CalcVector v) {//Метод, бъединеняющий два вектора в один
        return new CalcVector(this, v);//Объединение элементов одного вектора с элементами
        // другого вектора
    }

    public boolean lessThanVal(BigDecimal val) {//Метод для сравнения значения вектора с заданным
        // значением и определения, меньше ли оно.
        return elVector[0].compareTo(val) < 0;//Проверяет, является ли значение elVector[0] меньше
        // val. Если это верно (то есть compareTo вернуло отрицательное число), то метод lessThan
        // возвращает true, указывая на то, что первый элемент вектора меньше val. В противном случае
        // он вернет false.
    }

    public String toString() {//Метод переопределяет метод toString() из класса Object для создания
        // строки, представляющей текущий объект CalcVector.
        return Arrays.toString(elVector);//Преобразование массива элеменотов вектора в его
        // строковое представление.
    }
}
