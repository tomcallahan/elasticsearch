package org.elasticsearch.bootstrap;

import java.io.FilePermission;
import java.nio.file.Path;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class DataAwareDelegatingPermissionsCollection extends PermissionCollection {
    private final Path[] dataPaths;
    private final PermissionCollection delegate;
    private final CopyOnWriteArrayList<PermissionCollection> shortcutPermissions = new CopyOnWriteArrayList<>();
    public DataAwareDelegatingPermissionsCollection(Path[] dataPaths, PermissionCollection delegate) {
        this.dataPaths = dataPaths;
        this.delegate = delegate;
    }
    @Override
    public void add(Permission permission) {
        if (permission instanceof FilePermission) {
            FilePermission fp = (FilePermission) permission;
            System.out.println("Adding FilePermission: "+permission.getName());
            PermissionCollection pc = fp.newPermissionCollection();
            System.out.println("Data Paths: "+ Arrays.toString(dataPaths));
            for (Path p : dataPaths) {
                if (p.toString().equals(fp.getName()) || p.toString().equals(fp.getName().substring(0, fp.getName().length()-2))) {
                    shortcutPermissions.add(pc);
                    System.out.println("ShortCut: "+fp.getName());
                }
            }
        }
        delegate.add(permission);
    }

    @Override
    public boolean implies(Permission permission) {
        for (PermissionCollection pc : shortcutPermissions) {
            if (pc.implies(permission)) {
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
