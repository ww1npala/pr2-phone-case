package com.phonecase.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.phonecase.repository.*;
import com.phonecase.service.*;

/**
 * Guice DI-модуль. Реалізує патерни Dependency Injection та Inversion of Control.
 * Визначає прив'язки інтерфейсів до реалізацій.
 */

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        // singleton для підключення до db
        bind(DatabaseConnection.class).in(Singleton.class);
        bind(DatabaseInitializer.class).in(Singleton.class);

        // репозиторії (Data Layer) — патерн Repository
        bind(UserRepository.class).to(UserRepositoryImpl.class).in(Singleton.class);
        bind(PhoneModelRepository.class).to(PhoneModelRepositoryImpl.class).in(Singleton.class);
        bind(CategoryRepository.class).to(CategoryRepositoryImpl.class).in(Singleton.class);
        bind(DesignRepository.class).to(DesignRepositoryImpl.class).in(Singleton.class);
        bind(OrderRepository.class).to(OrderRepositoryImpl.class).in(Singleton.class);

        // сервіси (Business Logic Layer) — патерн Facade
        bind(UserService.class).to(UserServiceImpl.class).in(Singleton.class);
        bind(DesignService.class).to(DesignServiceImpl.class).in(Singleton.class);
        bind(OrderService.class).to(OrderServiceImpl.class).in(Singleton.class);
        bind(ReportService.class).to(ReportServiceImpl.class).in(Singleton.class);
    }
}