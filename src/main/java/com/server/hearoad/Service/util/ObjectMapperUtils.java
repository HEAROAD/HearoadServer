package com.server.hearoad.Service.util;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectMapperUtils {
    private static final ModelMapper modelMapper;

    static {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    private ObjectMapperUtils() {
    }

    public static <D, T> List<D> mapAll(final Collection<T> entityList, Class<D> outClass) {
        return entityList.stream()
                .map(entity -> map(entity, outClass))
                .collect(Collectors.toList());
    }

    public static <S, D> D map(final S source, Class<D> destinationClass) {
        return modelMapper.map(source, destinationClass);
    }

    public static <S, D> void map(final S source, D destination) {
        modelMapper.map(source, destination);
    }
}
