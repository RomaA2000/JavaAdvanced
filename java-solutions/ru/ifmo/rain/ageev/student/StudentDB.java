package ru.ifmo.ageev.student;

import info.kgeorgiy.java.advanced.student.AdvancedStudentGroupQuery;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements AdvancedStudentGroupQuery {

    private static final Comparator<Student> STUDENT_BY_NAME_COMPARATOR =
            Comparator.comparing(Student::getLastName)
                    .thenComparing(Student::getFirstName)
                    .thenComparingInt(Student::getId);

    private static final Comparator<Student> STUDENT_BY_ID_COMPARATOR = Comparator.naturalOrder();

    private static final String DEFAULT_STRING = "";

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupListFromCollection(students, STUDENT_BY_NAME_COMPARATOR);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupListFromCollection(students, STUDENT_BY_ID_COMPARATOR);
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getMaxGroupName(students,
                getNameReversedComparator(Comparator.comparingInt(group -> group.getStudents().size())));
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return maxWithComp(collectionToEntrySetStream(students, Student::getGroup,
                Collectors.mapping(Student::getFirstName, Collectors.collectingAndThen(Collectors.toSet(), Set::size))),
                Map.Entry.<String, Integer>comparingByValue().thenComparing(Map.Entry.<String, Integer>comparingByKey().reversed()));
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapCollectionToList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapCollectionToList(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return mapCollectionToList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapCollectionToList(students, this::fullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new TreeSet<>(getFirstNames(students));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students
                .stream()
                .min(STUDENT_BY_ID_COMPARATOR)
                .map(Student::getFirstName)
                .orElse(DEFAULT_STRING);
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortCollectionToList(students, STUDENT_BY_ID_COMPARATOR);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortCollectionToList(students, STUDENT_BY_NAME_COMPARATOR);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, final String name) {
        return findStudentsToList(students, equalsPredicateMaker(Student::getFirstName, name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, final String name) {
        return findStudentsToList(students, equalsPredicateMaker(Student::getLastName, name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, final String group) {
        return findStudentsToList(students, equalsPredicateMaker(Student::getGroup, group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return filterToCollection(students,
                equalsPredicateMaker(Student::getGroup, group),
                Comparator.naturalOrder(),
                Collectors.toMap(Student::getLastName, Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)));
    }

    private <E> List<E> mapStreamToList(Stream<Student> students, Function<Student, E> function) {
        return students
                .map(function)
                .collect(Collectors.toList());
    }

    private List<Student> sortStreamToList(Stream<Student> students, Comparator<Student> comparator) {
        return sortStreamToCollection(students, comparator, Collectors.toList());
    }

    private <E> E sortStreamToCollection(Stream<Student> students, Comparator<Student> comparator, Collector<Student, ?, E> collector) {
        return students
                .sorted(comparator)
                .collect(collector);
    }

    private <E> List<E> mapCollectionToList(Collection<Student> students, Function<Student, E> function) {
        return mapStreamToList(students.stream(), function);
    }

    private List<Student> sortCollectionToList(Collection<Student> students, Comparator<Student> comparator) {
        return sortStreamToList(students.stream(), comparator);
    }

    private <E> E filterToCollection(Collection<Student> students, Predicate<Student> predicate,
                                     Comparator<Student> comparator, Collector<Student, ?, E> collector) {
        return sortStreamToCollection(students
                .stream()
                .filter(predicate), comparator, collector);
    }

    private List<Student> findStudentsToList(Collection<Student> students, Predicate<Student> predicate) {
        return filterToCollection(students, predicate, STUDENT_BY_NAME_COMPARATOR, Collectors.toList());
    }

    private Predicate<Student> equalsPredicateMaker(Function<Student, String> func, final String result) {
        return s -> result.equals(func.apply(s));
    }

    private Stream<Group> getGroupStreamFromCollection(Collection<Student> students, Function<Map.Entry<String, List<Student>>, Group> constructorFunc) {
        return collectionToEntrySetStream(students, Student::getGroup, Collectors.toList())
                .map(constructorFunc);
    }

    private Function<Map.Entry<String, List<Student>>, Group> getConstructor(Function<Map.Entry<String, List<Student>>, List<Student>> func) {
        return (Map.Entry<String, List<Student>> map) ->
                new Group(map.getKey(),
                        func.apply(map));
    }

    private List<Group> getGroupListFromCollection(Collection<Student> students, Comparator<Student> comparator) {
        return getGroupStreamFromCollection(students,
                getConstructor(map ->
                        map.getValue()
                                .stream()
                                .sorted(comparator)
                                .collect(Collectors.toList())))
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
    }

    private String getMaxGroupName(Collection<Student> students, Comparator<Group> comparator) {
        return getGroupStreamFromCollection(students,
                getConstructor(Map.Entry::getValue))
                .max(comparator)
                .map(Group::getName)
                .orElse(DEFAULT_STRING);
    }

    private Comparator<Group> getNameReversedComparator(Comparator<Group> comparator) {
        return comparator.thenComparing(Comparator.comparing(Group::getName).reversed());
    }

    private String fullName(Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }

    private Stream<Student> mapFromIndexes(Collection<Student> parameters, final int[] array) {
        return Arrays.stream(array).mapToObj(List.copyOf(parameters)::get);
    }

    private <K, V> Stream<Map.Entry<K, V>> collectionToEntrySetStream(Collection<Student> students,
                                                                      Function<Student, K> func, Collector<Student, ?, V> collector) {
        return students
                .stream()
                .collect(Collectors.groupingBy(func, collector))
                .entrySet()
                .stream();
    }

    private <V> String maxWithComp(Stream<Map.Entry<String, V>> stream, Comparator<? super Map.Entry<String, V>> comp) {
        return stream.max(comp)
                .map(Map.Entry::getKey)
                .orElse(DEFAULT_STRING);
    }

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return maxWithComp(collectionToEntrySetStream(students,
                this::fullName,
                Collectors.mapping(Student::getGroup, Collectors.toSet())),
                Map.Entry.<String, Set<String>>comparingByValue(Comparator.comparingInt(Set::size))
                        .thenComparing(Map.Entry.comparingByKey(String::compareTo)));
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return mapStreamToListFromIndex(students, indices, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return mapStreamToListFromIndex(students, indices, Student::getLastName);
    }

    @Override
    public List<String> getGroups(Collection<Student> students, int[] indices) {
        return mapStreamToListFromIndex(students, indices, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return mapStreamToListFromIndex(students, indices, this::fullName);
    }

    private List<String> mapStreamToListFromIndex(Collection<Student> students, int[] indices, Function<Student, String> function) {
        return mapStreamToList(mapFromIndexes(students, indices), function);
    }
}
