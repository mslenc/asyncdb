package com.xs0.asyncdb.mysql.message.server;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ResultSetRowMessage implements ServerMessage, List<ByteBuf>, Cloneable {
    private ArrayList<ByteBuf> buffer;

    public ResultSetRowMessage() {
        buffer = new ArrayList<>();
    }

    private ResultSetRowMessage(ArrayList<ByteBuf> buffer) {
        this.buffer = buffer;
    }

    @Override
    public int kind() {
        return Row;
    }

    @Override
    public int size() {
        return buffer.size();
    }

    @Override
    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return buffer.contains(o);
    }

    @Override
    public int indexOf(Object o) {
        return buffer.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return buffer.lastIndexOf(o);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ResultSetRowMessage clone() {
        ResultSetRowMessage clone;
        try {
            clone = (ResultSetRowMessage) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        clone.buffer = (ArrayList<ByteBuf>) buffer.clone();

        return clone;
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return buffer.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(T[] a) {
        return buffer.toArray(a);
    }

    @Override
    public ByteBuf get(int index) {
        return buffer.get(index);
    }

    @Override
    public ByteBuf set(int index, ByteBuf element) {
        return buffer.set(index, element);
    }

    @Override
    public boolean add(ByteBuf byteBuf) {
        return buffer.add(byteBuf);
    }

    @Override
    public void add(int index, ByteBuf element) {
        buffer.add(index, element);
    }

    @Override
    public ByteBuf remove(int index) {
        return buffer.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        return buffer.remove(o);
    }

    @Override
    public void clear() {
        buffer.clear();
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends ByteBuf> c) {
        return buffer.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends ByteBuf> c) {
        return buffer.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return buffer.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return buffer.retainAll(c);
    }

    @NotNull
    @Override
    public ListIterator<ByteBuf> listIterator(int index) {
        return buffer.listIterator(index);
    }

    @NotNull
    @Override
    public ListIterator<ByteBuf> listIterator() {
        return buffer.listIterator();
    }

    @NotNull
    @Override
    public Iterator<ByteBuf> iterator() {
        return buffer.iterator();
    }

    @NotNull
    @Override
    public List<ByteBuf> subList(int fromIndex, int toIndex) {
        return buffer.subList(fromIndex, toIndex);
    }

    @Override
    public void forEach(Consumer<? super ByteBuf> action) {
        buffer.forEach(action);
    }

    @Override
    public Spliterator<ByteBuf> spliterator() {
        return buffer.spliterator();
    }

    @Override
    public boolean removeIf(Predicate<? super ByteBuf> filter) {
        return buffer.removeIf(filter);
    }

    @Override
    public void replaceAll(UnaryOperator<ByteBuf> operator) {
        buffer.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super ByteBuf> c) {
        buffer.sort(c);
    }

    @Override
    public boolean equals(Object o) {
        return buffer.equals(o);
    }

    @Override
    public int hashCode() {
        return buffer.hashCode();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return buffer.containsAll(c);
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    @Override
    public Stream<ByteBuf> stream() {
        return buffer.stream();
    }

    @Override
    public Stream<ByteBuf> parallelStream() {
        return buffer.parallelStream();
    }
}
