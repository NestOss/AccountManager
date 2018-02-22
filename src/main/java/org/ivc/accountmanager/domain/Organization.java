/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.naming.Name;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

/**
 * Organization class.
 *
 * @author Roman Osipov
 */
@Entry(objectClasses = {"organization", "top"}, base = Organization.BASE_DN)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Organization {

    //-------------------Logger---------------------------------------------------
    //-------------------Constants------------------------------------------------
    public static final int MAX_NAME_LENGTH  = 256;
    public static final String BASE_DN = "ou=organizations";
    public static final String NAME_PROPERTY_NAME = "name";
    public static final String O_ATTRIBUTE = "o";

    //-------------------Fields---------------------------------------------------
    @Id
    @JsonIgnore
    private Name dn;

    @Attribute(name = O_ATTRIBUTE)
    @DnAttribute(value = O_ATTRIBUTE, index = 1)
    @JsonProperty(NAME_PROPERTY_NAME)
    @NotBlank(message = "{blank.field}")
    @Length(max = MAX_NAME_LENGTH, message = "{length.field}")
    private String name;

    //-------------------Constructors---------------------------------------------
    /**
     * Creates an organization object.
     */
    public Organization() {
    }

    /**
     * Creates an organization object.
     *
     * @param name the name of organization.
     */
    public Organization(String name) {
        this.name = name;
    }

    //-------------------Getters and setters--------------------------------------
    /**
     * Returns the distinguished name.
     *
     * @return the distinguished name.
     */
    public Name getDn() {
        return dn;
    }

    /**
     * Sets the distinguished name.
     *
     * @param dn the distinguished name.
     */
    public void setDn(Name dn) {
        this.dn = dn;
    }

    /**
     * Returns the organization name.
     *
     * @return the organization name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the organization name.
     *
     * @param name the organization name.
     */
    public void setName(String name) {
        this.name = name;
    }

    //-------------------Methods--------------------------------------------------
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "dn");
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, "dn");
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
