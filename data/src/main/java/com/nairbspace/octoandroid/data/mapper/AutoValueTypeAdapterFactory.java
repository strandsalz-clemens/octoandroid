package com.nairbspace.octoandroid.data.mapper;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.nairbspace.octoandroid.data.entity.SlicingProgressEntity;
import com.nairbspace.octoandroid.domain.model.AutoGson;
import com.nairbspace.octoandroid.domain.model.SlicingProgress;

public class AutoValueTypeAdapterFactory implements TypeAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();

        // Only deserialize classes decorated with @AutoGson.
        AutoGson annotation = rawType.getAnnotation(AutoGson.class);
        if (annotation != null) {
            return (TypeAdapter<T>) gson.getAdapter(annotation.autoValueClass()).nullSafe();
        } else if (rawType.equals(SlicingProgressEntity.class)) {
            return (TypeAdapter<T>) SlicingProgressEntity.typeAdapter(gson).nullSafe();
        } else if (rawType.equals(SlicingProgress.class)) {
            return (TypeAdapter<T>) SlicingProgress.typeAdapter(gson).nullSafe();
        }
        return null;
    }

    /** Can implement code below if using custom type adapter */
//    @SuppressWarnings("unchecked")
//    @Override
//    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
//        Class<? super T> rawType = type.getRawType();
//        if (rawType.equals(Connection.class)) {
//            return (TypeAdapter<T>) Connection.typeAdapter(gson);
//        } else if (rawType.equals(Connection.Current.class)) {
//            return (TypeAdapter<T>) Connection.Current.typeAdapter(gson);
//        } else if (rawType.equals(Connection.Options.class)) {
//            return (TypeAdapter<T>) Connection.Options.typeAdapter(gson);
//        } else if (rawType.equals(Connection.PrinterProfile.class)) {
//            return (TypeAdapter<T>) Connection.PrinterProfile.typeAdapter(gson);
//        } else if (rawType.equals(Connect.class)) {
//            return (TypeAdapter<T>) Connect.typeAdapter(gson);
//        }
//        return null;
//    }
}
