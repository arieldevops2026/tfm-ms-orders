package dev.ariel.tfm.orders.util.properties;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app")
public interface AppProps {

    String version();

    String environment();
}
