package org.start2do.entity.security;

import io.ebean.Model;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.JsonIgnore;
import io.ebean.annotation.StorageEngine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.constant.DBConstant;
import org.start2do.util.Md5Util;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@DbComment("权限表")
@Table(name = "sys_permission")
@StorageEngine("ENGINE = MergeTree() order by id;")
public class SysPermission extends Model {

    @Id
    @Column(length = DBConstant.TITLE_LENGTH)
    private String id;
    @Column(length = 4096)
    private String url;

    private boolean pass;
    @JsonIgnore
    @com.fasterxml.jackson.annotation.JsonIgnore
    @JoinTable(name = "sys_permission_role_ref", joinColumns = {
        @JoinColumn(name = "permission_id", referencedColumnName = "id")}, inverseJoinColumns = {
        @JoinColumn(name = "role_id", referencedColumnName = "id")})
    @ManyToMany(fetch = FetchType.LAZY)
    public List<SysRole> roles;
    @JsonIgnore
    @com.fasterxml.jackson.annotation.JsonIgnore
    @JoinTable(name = "sys_permission_user_ref", joinColumns = {
        @JoinColumn(name = "permission_id", referencedColumnName = "id")}, inverseJoinColumns = {
        @JoinColumn(name = "user_id", referencedColumnName = "id")})
    @ManyToMany(fetch = FetchType.LAZY)
    private List<SysUser> users;
    @Column(length = DBConstant.TITLE_LENGTH)
    private String groupName;

    public SysPermission(String url) {
        this.id = Md5Util.md5(url);
        this.url = url;
    }

    public SysPermission(String groupName, String url, boolean pass) {
        this.groupName = groupName;
        this.id = Md5Util.md5(url);
        this.url = url;
        this.pass = pass;
    }
}
