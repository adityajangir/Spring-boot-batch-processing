package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class Processorinitial implements ItemProcessor<NoSqlDataModel, NoSqlDataModel> {
    private static final Logger log = LoggerFactory.getLogger(StudentItemProcessor.class);

    @Override
    public NoSqlDataModel process(NoSqlDataModel stud) {
        final String firstName = stud.getFirstName().toUpperCase();
        final String lastName = stud.getLastName().toUpperCase();
        final String course = stud.getCourse().toUpperCase();
        final NoSqlDataModel transformedStudent = new NoSqlDataModel(stud.getId(),firstName, lastName, course);

        log.info("Converting (" + stud + ") into (" + transformedStudent + ")");
        return transformedStudent;
    }
}