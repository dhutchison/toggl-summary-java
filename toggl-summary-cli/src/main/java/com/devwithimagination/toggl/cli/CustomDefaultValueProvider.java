package com.devwithimagination.toggl.cli;

import java.time.LocalDate;

import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.PropertiesDefaultProvider;

/**
 * Extension of {@link picocli.CommandLine.PropertiesDefaultProvider}
 * which will handle date parameters without a default. 
 * 
 */
class CustomDefaultValueProvider extends PropertiesDefaultProvider {

    @Override
    public String defaultValue(ArgSpec argSpec) throws Exception {

        String defaultValue = super.defaultValue(argSpec);

        if (defaultValue == null && LocalDate.class.isAssignableFrom(argSpec.type())) {
            defaultValue = LocalDate.now().toString();
        }

        return defaultValue;
    }

}
