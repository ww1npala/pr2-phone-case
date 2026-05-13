package com.phonecase.model;

import java.time.LocalDateTime;

/**
 * Базова сутність. Реалізує принцип наслідування (ООП).
 * Містить спільне поле id для всіх сутностей.
 */
public abstract class BaseEntity {


    protected Integer id;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
}