package com.quicktutorialz.nio.custom.observables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Flowable {

    private Supplier supplier;
    private List<Function> mapList = new ArrayList<>();
    ExecutorService exec = Executor.getExecutor(); //Executors.newFixedThreadPool(10);

    /* lazy elaboration */

    public Flowable just(Supplier supplier){
        this.supplier = supplier;
        return this;
    }

    public Flowable map(Function function){
        mapList.add(function);
        return this;
    }

    /* -- lazy subscription -- */

    public void subscribe(Consumer onNext){
        onNext.accept(getResult(supplier.get()));
    }

    public void subscribe(Consumer onNext, Consumer onError, Advicer onFinish){
        try {
            Object source = supplier.get();
            if (source instanceof Iterable) {
                Iterable iterable = (Iterable) source;
                Iterator iterator = iterable.iterator();
                while (iterator.hasNext()) {
                    Object result = getResult(iterator.next());
                    onNext.accept(result);
                }
                onFinish.advice();
            } else {
                Object result = getResult(source);
                onNext.accept(result);
            }
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    public void subscribe(Consumer onNext, Consumer onError){
        try {
            Object source = supplier.get();
            if (source instanceof Iterable) {
                Iterable iterable = (Iterable) source;
                Iterator iterator = iterable.iterator();
                while (iterator.hasNext()) {
                    Object result = getResult(iterator.next());
                    onNext.accept(result);
                }
            } else {
                Object result = getResult(source);
                onNext.accept(result);
            }
        } catch (Exception e) {
            onError.accept(e);
        }
    }


    /* -- lazy and async subscription -- */

    public void subscribeAsync(Consumer onNext){
        exec.submit(() -> subscribe(onNext) );
    }

    public void subscribeAsync(Consumer onNext, Consumer onError, Advicer onFinish){
        exec.submit(() -> subscribe(onNext, onError, onFinish));
    }

    public void subscribeAsync(Consumer onNext, Consumer onError){
        exec.submit(() -> subscribe(onNext, onError));
    }

    public void subscribeAsync(Consumer onNext, ExecutorService exec){
        exec.submit(() -> subscribe(onNext));
    }

    public void subscribeAsync(Consumer onNext, Consumer onError, Advicer onFinish, ExecutorService exec){
        exec.submit(() -> subscribe(onNext, onError, onFinish));
    }

    public void subscribeAsync(Consumer onNext, Consumer onError, ExecutorService exec){
        exec.submit(() -> subscribe(onNext, onError));
    }


    private Object getResult(Object source){
        Object result = source;
        for(Function f : mapList){
            result = f.apply(result);
            if(result instanceof Observable){
                PartialResult partial = new PartialResult();
                ((Observable) result).subscribe(res -> partial.setResult(res));
                result = partial.get();
            }else if(result instanceof Flowable){
                PartialResult partial = new PartialResult();
                ((Flowable) result).subscribe(res -> partial.setResult(res));
                result = partial.get();
            }
        }
        return result;
    }

}