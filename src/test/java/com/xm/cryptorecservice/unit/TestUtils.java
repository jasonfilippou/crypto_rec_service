package com.xm.cryptorecservice.unit;

import com.google.common.collect.Comparators;
import com.xm.cryptorecservice.util.SortOrder;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class TestUtils {

    public static boolean collectionIsSortedByFieldInGivenDirection(
            Collection<?> pojos, String sortByField, SortOrder sortOrder) {
        // Using a Guava dependency here
        return Comparators.isInOrder(
                pojos,
                (p1, p2) ->
                        compareFieldsInGivenOrder(p1, p2, sortByField, sortOrder));
    }

    public static <T extends Comparable<T>> int compareFieldsInGivenOrder(
            Object pojoOne, Object pojoTwo, String sortByField, SortOrder sortOrder) {
        try {
            assert pojoOne.getClass().equals(pojoTwo.getClass());
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(sortByField, pojoOne.getClass());
            Method appropriateGetter = propertyDescriptor.getReadMethod();
            @SuppressWarnings("unchecked")
            T pojoOneFieldValue = (T) appropriateGetter.invoke(pojoOne);
            @SuppressWarnings("unchecked")
            T pojoTwoFieldValue = (T) appropriateGetter.invoke(pojoTwo);
            return sortOrder == SortOrder.ASC
                    ? pojoOneFieldValue.compareTo(pojoTwoFieldValue)
                    : pojoTwoFieldValue.compareTo(pojoOneFieldValue);
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        } catch (ClassCastException exc) {
            throw new RuntimeException(
                    "Field " + sortByField + " of " + pojoOne.getClass().getSimpleName() + " is not Comparable.");
        }
    }
}
