import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class Main {
    //Этот класс представляет собой реализацию различных функций для решения дифференциальных уравнений
    // методом Рунге-Кутты четвертого порядка.
    enum SetMode {//Используется для выбора между параллельным и последовательным выполнением некоторых
        // вычислений, в зависимости от того, включен ли режим или отключен
        DISABLE, ENABLE
    }

    final static BigDecimal MAX_Q = new BigDecimal("0.05");
    static int SCALE = 30;

    static boolean checkStep(BigDecimal k_1, BigDecimal k_2, BigDecimal k_3, BigDecimal k_4) {//Метод
        // используется, чтобы сравнить значения k_1, k_2, k_3, и k_4 с MAX_Q. Если разница между k_4 и k_3,
        // деленная на разницу между k_1 и k_2, меньше MAX_Q, то метод вернет true
        return (k_4.subtract(k_3).divide(k_1.subtract(k_2), RoundingMode.HALF_UP)).abs().compareTo(MAX_Q) < 0; // Q < MAX_Q
    }

    static boolean checkAbsDiff(BigDecimal yXn, BigDecimal yn, BigDecimal yn_) {//Метод проверяет,
        // является ли абсолютная разница между yXn и yn_ меньше, чем 1/15 от абсолютной разницы
        // между yn и yn_. Если это так, это указывает на определенный уровень точности
        return yXn.subtract(yn_).abs().compareTo(yn.subtract(yn_).abs().multiply(BigDecimal.valueOf(1. / 15.))) < 0;
    }

    static void setScale(int scale) {//Метод вызывается, устанавливлиапющий значение переменной
        // SCALE равным переданному scale
        SCALE = scale;
    }

    public static Map<BigDecimal, CalcVector> resolve(CalcEquation equation, SetMode mode) {//Метод
        // используется для решения уравнений и создания таблицы значений, которые соответствуют
        // решению этого уравнения на определенном интервале
        Function<CalcVector, BigDecimal>[] arrFunctions = equation.getArrFunctions();
        BigDecimal x_0 = equation.getX_0();
        CalcVector y_0 = equation.getY_0();
        BigDecimal step = equation.getStep();
        BigDecimal maxX = equation.getMaxX();

        MathContext mc = new MathContext(SCALE);

        CalcVector x = new CalcVector(x_0);
        CalcVector y = new CalcVector(y_0);
        Map<BigDecimal, CalcVector> table = new HashMap<>();//Создает пустую хэш-таблицу для хранения
        // результатов вычислений
        while (x.lessThanVal(maxX)) {//Цикл, который продолжается, пока x меньше, чем maxX.
            if (mode == SetMode.DISABLE)
                y = getNext(y, x, arrFunctions, step);//Вызывается метод getNext(y, x, fs, h) для
                // вычисления следующего значения y
            else {
                try {
                    y = getNextParallel(y, x, arrFunctions, step);//Вызывается метод
                    // getNextParallel(y, x, fs, h) для параллельного вычисления следующего значения y
                    // с использованием многопоточности.
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();//В случае ошибок выводит стек вызова исключения.
                }
            }
            x = x.addVectorNumber(step);//Увеличивает x на шаг.
            table.put(x.getС(0).round(mc), y.applyFunction(t -> t.round(mc)));//Добавляет в
            // хэш-таблицу результаты вычислений для текущего x и y, округленные в соответствии с
            // MathContext
        }
        return table;
    }

    static void testStep(CalcEquation eq, CalcVector ys) {//Метод проверяет, как изменение значения
        // шага влияет на решение уравнения и печатает разницу между решениями с текущим и уменьшенным
        // вдвое шагом
        BigDecimal step = eq.getStep();//Получает текущее значение шага из объекта eq.
        BigDecimal step_2 = step.divide(new BigDecimal("2"), RoundingMode.HALF_UP);//Вычисляет половину
        // значения шага и сохраняет его в переменной step_2. Используется округление в половину вверх
        CalcEquation nEq = new CalcEquation(eq);//Создает копию объекта eq и сохраняет ее в переменной nEq
        nEq.setStep(step_2);//Устанавливает новое значение шага step_2 для объекта nEq
        //Вычисляет значение вектора y_n путем решения уравнения eq с отключенным режимом параллельных
        // вычислений и получением результата при максимальном значении x для eq
        CalcVector y_n = new CalcVector(resolve(eq, SetMode.DISABLE).get(eq.getMaxX())), yn_ = new CalcVector(resolve(nEq, SetMode.DISABLE).get(nEq.getMaxX()));
        //В цикле происходит сравнение абсолютных разниц между соответствующими компонентами векторов
        // ys, y_n и yn_. Результаты сравнения выводятся на экран
        for (int i = 0; i < y_n.sizeVector(); i++)
            System.out.println("" + i + ' ' + checkAbsDiff(ys.getС(i), y_n.getС(i), yn_.getС(i)));
    }

    static CalcVector getNextParallel(CalcVector prevVector, CalcVector x, final Function<CalcVector, BigDecimal>[] arrFunctions, BigDecimal step)
            throws ExecutionException, InterruptedException {//Метод реализует метод Рунге-Кутта
        // четвёртого порядка для решения дифференциальных уравнений.
        // Он вычисляет значения k_1, k_2, k_3, k_4 и использует их для вычисления следующего значения
        // вектора y.
        BigDecimal val2 = new BigDecimal(2), val6 = new BigDecimal(6);
        BigDecimal step_2 = step.divide(val2, SCALE, RoundingMode.HALF_UP);
        CalcVector xS = CalcVector.addVectorNumber(x, step), xS_2 = CalcVector.addVectorNumber(x, step_2);

        class RKMethod {//Класс, содержащий методы для вычисления
            CalcVector compK_1() {
                try {
                    return CalcVector.applyFunctions(CalcVector.mergeVectors(x, prevVector), arrFunctions);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            CalcVector compK_23(CalcVector prevK) throws ExecutionException, InterruptedException {
                CalcVector k_23S = CalcVector.multiply(prevK, step_2);
                return CalcVector.applyFunctions(CalcVector.mergeVectors(xS_2, CalcVector.addVectors(prevVector, k_23S)), arrFunctions);
            }

            CalcVector compK_4(CalcVector k_3) throws ExecutionException, InterruptedException {
                CalcVector k_3S = CalcVector.multiply(k_3, step);
                return CalcVector.applyFunctions(CalcVector.mergeVectors(xS, CalcVector.addVectors(prevVector, k_3S)), arrFunctions);
            }

            CalcVector compDy(CalcVector k_1, CalcVector k_2, CalcVector k_3, CalcVector k_4)
                    throws ExecutionException, InterruptedException {
                return CalcVector.multiply(
                        CalcVector.addVectors(k_1, CalcVector.multiply(k_2, BigDecimal.valueOf(2)), CalcVector.multiply(k_3, BigDecimal.valueOf(2)), k_4),
                        step.divide(val6, SCALE, RoundingMode.HALF_UP));
            }
        }

        RKMethod rk = new RKMethod();
        CalcVector k_1 = rk.compK_1();

        CalcVector k_2 = rk.compK_23(k_1);

        CalcVector k_3 = rk.compK_23(k_2);

        CalcVector k_4 = rk.compK_4(k_3);

        try {
            return prevVector.addVectors(rk.compDy(k_1, k_2, k_3, k_4));//Вычисляется следующее значение
            // вектора y как сумма prevVector и dy, где dy вычисляется вызовом метода compDy с
            // использованием k_1, k_2, k_3 и k_4.
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Реализует метод Рунге-Кутта четвёртого порядка для решения дифференциальных уравнений.
    // Он использует вычисленные k_1, k_2, k_3, и k_4 для определения следующего значения вектора y.
    static CalcVector getNext(CalcVector prevVector, CalcVector x, final Function<CalcVector, BigDecimal>[] arrFunctions, BigDecimal step) {
        CalcVector k_1, k_2, k_3, k_4;
        BigDecimal val2 = new BigDecimal(2), val6 = new BigDecimal(6);
        BigDecimal step_2 = step.divide(val2, SCALE, RoundingMode.HALF_UP);

        k_1 = x.mergeVectors(prevVector).applyArrFuToElVectors(arrFunctions);

        k_2 = x.addVectorNumber(step_2).mergeVectors(prevVector.addVectors(k_1.multiply(step_2))).applyArrFuToElVectors(arrFunctions);

        k_3 = x.addVectorNumber(step_2).mergeVectors(prevVector.addVectors(k_2.multiply(step_2))).applyArrFuToElVectors(arrFunctions);

        k_4 = x.addVectorNumber(step).mergeVectors(prevVector.addVectors(k_3.multiply(step))).applyArrFuToElVectors(arrFunctions);

        CalcVector dy = k_1.addVectors(k_2.multiply(val2)).addVectors(k_3.multiply(val2)).addVectors(k_4).multiply(step.divide(val6, SCALE, RoundingMode.HALF_UP));
        return prevVector.addVectors(dy);//Вычисляется следующее значение вектора y как сумма
        // prevVector и dy.
    }

    private static void printResult(Map<BigDecimal, CalcVector> result) {
        ArrayList<BigDecimal> keys = new ArrayList<>(result.keySet());
        keys.sort(BigDecimal::compareTo);
        for (BigDecimal k: keys)
            System.out.println(k + " : " + result.get(k));
    }

    static Map<BigDecimal, CalcVector> testDE(CalcEquation eq, SetMode mode) {
        long start = System.currentTimeMillis();//Записывает текущее время в миллисекундах в
        // переменную start до начала вычислений.
        Map<BigDecimal, CalcVector> result = resolve(eq, mode);//Вызывает метод resolve с переданным
        // уравнением eq и режимом mode для решения дифференциального уравнения. Результат решения
        // сохраняется в переменной result.
        long finish = System.currentTimeMillis();//Записывает текущее время в миллисекундах в
        // переменную finish после завершения вычислений.
        printResult(result);//Вызывает метод printResult, который печатает результат решения дифференциального уравнения.
        long elapsed = finish - start;//Вычисляет разницу между временем finish и start, чтобы
        // определить, сколько времени заняли вычисления
        System.out.println("Total time(ms): " + elapsed);//Выводит в консоль общее время выполнения
        // вычислений в миллисекундах
        return result;
    }

    static CalcEquation getEquation1() {
        Function<CalcVector, BigDecimal>[] arrFunctions = new Function[2];//Создается массив функций,
        // который будет содержать две функции. Эти функции представляют собой правые части
        // дифференциальных уравнений.
        //В первом элементе массива определяется функция, которая вычисляет первое уравнение.
        // Эта функция умножает первый элемент вектора на -3 и вычитает второй элемент.
        arrFunctions[0] = vector -> vector.getС(1).multiply(BigDecimal.valueOf(-3)).subtract(vector.getС(2));
        //Во втором элементе массива определяется функция, которая вычисляет второе уравнение.
        // Эта функция просто вычитает второй элемент вектора из первого элемента.
        arrFunctions[1] = vector -> vector.getС(1).subtract(vector.getС(2));
        BigDecimal[] y_0S = {BigDecimal.valueOf(2), BigDecimal.valueOf(-1)};//Создается массив, который
        // содержит начальные условия для системы уравнений. В данном случае, начальные значения для
        // y_1 и y_2 задаются как 2 и -1 соответственно.
        //Создается и возвращается объект CalcEquation, который представляет дифференциальное уравнение. В этом объекте указываются:
        //В этом объекте указываются:
        //- Массив функций, которые определяют правые части уравнений.
        //- Начальное время (в данном случае, 0).
        //- Начальное состояние системы, представленное вектором CalcVector с начальными
        // значениями из y_0S.
        //- Шаг интегрирования (0.01) - это шаг, с которым будут вычисляться значения.
        //- Время окончания (0.5) - это время, до которого будут проводиться вычисления.
        return new CalcEquation(arrFunctions, BigDecimal.valueOf(0), new CalcVector(y_0S), BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.5));
    }

    static CalcEquation getEquation2() {
        Function<CalcVector, BigDecimal>[] arrFunctions = new Function[4];//Создается массив функций
        // который будет содержать четыре функции. Эти функции представляют собой правые части
        // дифференциальных уравнений.
        arrFunctions[0] = vector -> vector.getС(1).multiply(BigDecimal.valueOf(-3)).subtract(vector.getС(2));
        arrFunctions[1] = vector -> vector.getС(1).subtract(vector.getС(2));
        arrFunctions[2] = vector -> vector.getС(0).multiply(vector.getС(2)).multiply(vector.getС(1));
        arrFunctions[3] = vector -> vector.getС(1).subtract(vector.getС(0)).multiply(vector.getС(1));
        //Создается массив y_0S, который содержит начальные условия для системы уравнений.
        //В данном случае, начальные значения для y_1, y_2, y_3 и y_4 задаются соответственно как 2, -1, 12.5 и 0.
        BigDecimal[] y_0S = {BigDecimal.valueOf(2), BigDecimal.valueOf(-1), BigDecimal.valueOf(12.5), BigDecimal.valueOf(0)};
        //Создается и возвращается объект CalcEquation, который представляет дифференциальное уравнение.
        //В этом объекте указываются:
        //- Массив функций arrFunctions, которые определяют правые части уравнений.
        //- Начальное время (в данном случае, 0).
        //- Начальное состояние системы, представленное вектором CalcVector с начальными значениями из y_0S.
        //- Шаг интегрирования (0.01) - это шаг, с которым будут вычисляться значения.
        //- Время окончания (1) - это время, до которого будут проводиться вычисления.
        return new CalcEquation(arrFunctions, BigDecimal.valueOf(0), new CalcVector(y_0S), BigDecimal.valueOf(0.01), BigDecimal.valueOf(1));
    }

    public static void main(String[] args) {
        System.out.println("Let's wait...\n");
        System.out.println("First system of equations\n");
        System.out.println("\n=======================================\n");
        System.out.println("==============PARALLEL OFF==============\n");
        System.out.println("=======================================\n");
        setScale(1000); //our accuracy
        CalcEquation eq_1 = getEquation1();
        CalcEquation eq_2 = getEquation2();
        testDE(eq_1, SetMode.DISABLE);
        System.out.println("\n=======================================\n");
        System.out.println("==============PARALLEL ON===============\n");
        System.out.println("=======================================\n");
        testDE(eq_1, SetMode.ENABLE);
        System.out.println("\n=======================================\n");
        System.out.println("\nSecond system of equations\n");
        System.out.println("\n=======================================\n");
        System.out.println("==============PARALLEL OFF==============\n");
        System.out.println("=======================================\n");
        testDE(eq_2, SetMode.DISABLE);
        System.out.println("\n=======================================\n");
        System.out.println("==============PARALLEL ON===============\n");
        System.out.println("=======================================\n");
        testDE(eq_2, SetMode.ENABLE);
    }
}
