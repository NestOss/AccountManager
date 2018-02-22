/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Set;
import javax.naming.Name;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

/**
 * Group class.
 *
 * @author Sokolov@ivc.org
 */
@Entry(objectClasses = {"groupOfNames", "top"}, base = Group.BASE_DN)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {
    //-------------------Logger---------------------------------------------------

    //-------------------Constants------------------------------------------------
    @JsonIgnore
    public static final String BASE_DN = "ou=groups";
    public static final String COMMON_NAME_PROPERTY = "commonName";
    public static final String MEMBER_ATTRIBUTE = "member";
    public static final String MEMBERS_PROPERTY = "members";
    public static final String CN_ATTRIBUTE = "cn";

    //-------------------Fields---------------------------------------------------
    @Id
    @JsonIgnore
    private Name dn;

    @Attribute(name = CN_ATTRIBUTE)
    @DnAttribute(value = CN_ATTRIBUTE, index = 1)
    @JsonProperty(COMMON_NAME_PROPERTY)
    @NotBlank(message = "{blank.field}")
    private String commonName;

    @Attribute(name = MEMBER_ATTRIBUTE)
    @JsonIgnore
    private Set<Name> members = new HashSet<>();

    //-------------------Constructors---------------------------------------------
    /**
     * Create a group object.
     */
    public Group() {
    }

    /**
     * Create a group object.
     *
     * @param commonName the common name of the group.
     */
    public Group(String commonName) {
        this.commonName = commonName;
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
     * Returns the common name.
     *
     * @return the common name.
     */
    public String getCommonName() {
        return commonName;
    }

    /**
     * Sets the common name.
     *
     * @param commonName the common name
     */
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * Returns a list of the group's member
     *
     * @return list of the member of the group.
     */
    public Set<Name> getMembers() {
        return members;
    }

    /**
     * Sets the list of the group's member
     *
     * @param members list of the group's member.
     */
    public void setMembers(Set<Name> members) {
        this.members = members;
    }

    //-------------------Methods--------------------------------------------------
    /**
     * Added member to the group.
     *
     * @param member adding member.
     */
    public void addMember(Name member) {
        members.add(member);
    }

    /**
     * removed group's member from the group.
     *
     * @param member removing member.
     */
    public void removeMember(Name member) {
        members.remove(member);
    }

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
