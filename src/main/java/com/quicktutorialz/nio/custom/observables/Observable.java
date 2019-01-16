package com.quicktutorialz.nio.custom.observables;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Observable {

    private Supplier supplier;
    private List<Function> mapList = new ArrayList<>();
    ExecutorService exec = Executor.getExecutor(); //Executors.newFixedThreadPool(10);

    /* lazy elaboration */

    public Observable just(Supplier supplier){
        this.supplier = supplier;
        return this;
    }

    public Observable map(Function function){
        mapList.add(function);
        return this;
    }

    /* -- lazy subscription -- */

    public void subscribe(Consumer onFinish){
        onFinish.accept(getResult());
    }

    public void subscribe(Consumer onFinish, Consumer onError){
        try{
            onFinish.accept(getResult());
        }catch(Exception e){
            onError.accept(e);
        }
    }

    /* -- lazy and async subscription -- */

    public void subscribeAsync(Consumer onFinish){
        exec.submit(() -> subscribe(onFinish));
    }

    public void subscribeAsync(Consumer onFinish, Consumer onError){
        exec.submit(() -> subscribe(onFinish, onError));
    }

    public void subscribeAsync(Consumer onFinish, ExecutorService exec){
        exec.submit(() -> subscribe(onFinish));
    }

    public void subscribeAsync(Consumer onFinish, Consumer onError, ExecutorService exec){
        exec.submit(() -> subscribe(onFinish, onError));
    }


    private Object getResult(){
        Object result = supplier.get();
        for(Function f : mapList){
            result = f.apply(result);
            if(result instanceof Observable){
                PartialResult partial = new PartialResult();
                ((Observable) result).subscribe(res -> partial.setResult(res));
                result = partial.get();
            }
        }
        return result;
    }

}