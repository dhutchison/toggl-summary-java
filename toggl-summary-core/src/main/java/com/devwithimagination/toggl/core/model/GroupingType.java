package com.devwithimagination.toggl.core.model;

/**
 * Enum holding the types of grouping the API could use. While we parse this, we largely 
 * expect to use the client grouping, with project subgrouping. 
 */
public enum GroupingType {
    CLIENT,
    PROJECT,
    USER,
    UNKNOWN
}
