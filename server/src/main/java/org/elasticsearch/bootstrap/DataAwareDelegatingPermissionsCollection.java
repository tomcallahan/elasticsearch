package org.elasticsearch.bootstrap;

import java.io.FilePermission;
import java.nio.file.Path;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class DataAwareDelegatingPermissionsCollection extends PermissionCollection {
    private final Path[] dataPaths;
    private final PermissionCollection delegate;
    private final CopyOnWriteArrayList<Permission> shortcutPermissions = new CopyOnWriteArrayList<>();
    public DataAwareDelegatingPermissionsCollection(Path[] dataPaths, PermissionCollection delegate) {
        this.dataPaths = dataPaths;
        this.delegate = delegate;
    }
    @Override
    public void add(Permission permission) {
        if (permission instanceof FilePermission) {
            FilePermission fp = (FilePermission) permission;
            PermissionCollection pc = fp.newPermissionCollection();
            for (Path p : dataPaths) {
                if (p.toString().startsWith(fp.getName())) {
                    shortcutPermissions.add(permission);
                }
            }
        }
        delegate.add(permission);
    }

    @Override
    public boolean implies(Permission permission) {
        for (Permission p : shortcutPermissions) {
            if (p.implies(permission)) {
                return true;
            }
        }
        return delegate.implies(permission);
    }

    @Override
    public Enumeration<Permission> elements() {
        return delegate.elements();
    }
}
