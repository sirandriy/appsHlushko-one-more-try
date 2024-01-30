package ua.edu.ucu.stream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ua.edu.ucu.function.*;

public class AsIntStream implements IntStream {
    private List<Integer> values;
    private List<Operation> operations;

    private interface Operation {
        Iterator<Integer> apply(Iterator<Integer> iterator);
    }

    private Iterator<Integer> iterator() {
        Iterator<Integer> iterator = values.iterator();
        for (Operation operation : operations) {
            iterator = operation.apply(iterator);
        }
        return iterator;
    }

    private AsIntStream(int... values) {
        this.values = new ArrayList<>();
        this.operations = new ArrayList<>();
        for (int val : values) {
            this.values.add(val);
        }
    }

    public static IntStream of(int... values) {
        return new AsIntStream(values);
    }

    @Override
    public Double average() {
        if (values.isEmpty()){
            throw new IllegalArgumentException("Stream is empty");
        }

        int sum = 0;
        for (int value : values){
            sum += value;
        }
        return (double) sum / values.size();
    }

    @Override
    public Integer max() {
        if (values.isEmpty()){
            throw new IllegalArgumentException("Stream is empty");
        }

        Integer max = values.get(0);
        for (int value : values){
            if (value > max){
                max = value;
            }
        }
        return max;
    }

    @Override
    public Integer min() {
        if (values.isEmpty()){
            throw new IllegalArgumentException("Stream is empty");
        }

        Integer min = values.get(0);
        for (int value : values){
            if (value < min){
                min = value;
            }
        }
        return min;
    }

    @Override
    public long count() {
        return values.size();
    }

    @Override
    public Integer sum() {
        if (values.isEmpty()){
            throw new IllegalArgumentException("Stream is empty");
        }
        int sum = 0;
        for (int value : values){
            sum += value;
        }
        return sum;
    }

    @Override
    public IntStream filter(IntPredicate predicate) {
        operations.add(new FilterOperation(predicate));
        return this;
    }

    private static class FilterOperation implements Operation {
        private IntPredicate predicate;

        public FilterOperation(IntPredicate predicate) {
            this.predicate = predicate;
        }

        @Override
        public Iterator<Integer> apply(Iterator<Integer> iterator) {
            return new Iterator<Integer>() {
                private Integer nextValue = null;

                @Override
                public boolean hasNext() {
                    while (iterator.hasNext()) {
                        Integer value = iterator.next();
                        if (predicate.test(value)) {
                            nextValue = value;
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public Integer next() {
                    if (!hasNext()) {
                        throw new IllegalStateException("No more elements");
                    }
                    Integer result = nextValue;
                    nextValue = null;
                    return result;
                }
            };
        }
    }

    private int[] toIntArray(List<Integer> list){
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    @Override
    public void forEach(IntConsumer action) {
        for (int value: values){
            action.accept(value);
        }
    }

    @Override
    public IntStream map(IntUnaryOperator mapper) {
        operations.add(new MapOperation(mapper));
        return this;
    }

    private static class MapOperation implements Operation {
        private IntUnaryOperator mapper;

        public MapOperation(IntUnaryOperator mapper) {
            this.mapper = mapper;
        }

        @Override
        public Iterator<Integer> apply(Iterator<Integer> iterator) {
            return new Iterator<Integer>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Integer next() {
                    if (!hasNext()) {
                        throw new IllegalStateException("No more elements");
                    }
                    return mapper.apply(iterator.next());
                }
            };
        }
    }

    @Override
    public IntStream flatMap(IntToIntStreamFunction func) {
        operations.add(new FlatMapOperation(func));
        return this;
    }

    private class FlatMapOperation implements Operation {
        private IntToIntStreamFunction func;

        public FlatMapOperation(IntToIntStreamFunction func) {
            this.func = func;
        }

        @Override
        public Iterator<Integer> apply(Iterator<Integer> iterator) {
            return new Iterator<Integer>() {
                private Iterator<Integer> currentIterator = null;

                @Override
                public boolean hasNext() {
                    while ((currentIterator == null || !currentIterator.hasNext()) && iterator.hasNext()) {
                        int value = iterator.next();
                        List<Integer> result = flatMapToIntStream(value);
                        currentIterator = result.iterator();
                    }
                    return currentIterator != null && currentIterator.hasNext();
                }

                @Override
                public Integer next() {
                    if (!hasNext()) {
                        throw new IllegalStateException("No more elements");
                    }
                    return currentIterator.next();
                }

                private List<Integer> flatMapToIntStream(int value) {
                    List<Integer> result = new ArrayList<>();
                    IntStream intStream = func.applyAsIntStream(value);
                    intStream.forEach(result::add);
                    return result;
                }
            };
        }
    }

    @Override
    public int reduce(int identity, IntBinaryOperator op) {
        int result = identity;
        for (int val : values) {
            result = op.apply(result, val);
        }
        return result;
    }

    @Override
    public int[] toArray() {
        return toIntArray(values);
    }
}
