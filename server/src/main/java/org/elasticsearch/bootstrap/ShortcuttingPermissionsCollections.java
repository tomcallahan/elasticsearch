package org.elasticsearch.bootstrap;

import java.io.FilePermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.concurrent.CopyOnWriteArrayList;

public class ShortcuttingPermissionsCollections extends PermissionCollection {
    private final CopyOnWriteArrayList<PermissionCollection> perms = new CopyOnWriteArrayList<>();
    private final PermissionCollection delegate;
    public ShortcuttingPermissionsCollections(PermissionCollection delegate) {
        this.delegate = delegate;
    }
    @Override
    public void add(Permission permission) {
        if (permission instanceof FilePermission) {
            FilePermission fp = (FilePermission) permission;
            PermissionCollection pc = fp.newPermissionCollection();
            pc.add(fp);
            perms.add(pc);
        }
        delegate.add(permission);
    }

    @Override
    public boolean implies(Permission permission) {
        if (permission instanceof FilePermission) {
            for (PermissionCollection pc : perms) {
                if (pc.implies(permission)) {
                    return true;
                }
            }
        }
        return delegate.implies(permission);
    }

    @Override
    public Enumeration<Permission> elements() {
        return delegate.elements();
    }
}
