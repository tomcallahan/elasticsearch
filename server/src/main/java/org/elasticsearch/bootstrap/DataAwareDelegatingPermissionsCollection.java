package org.elasticsearch.bootstrap;

import java.io.FilePermission;
import java.nio.file.Path;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

class DataAwareDelegatingPermissionsCollection extends PermissionCollection {
    private final Path[] dataPaths;
    private final PermissionCollection delegate;
    private final CopyOnWriteArrayList<PermissionCollection> shortcutPermissions = new CopyOnWriteArrayList<>();
    Random r = new Random();
    private final AtomicLong counter = new AtomicLong(0);
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
            pc.add(fp);
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
        if (permission instanceof FilePermission) {
            for (PermissionCollection pc : shortcutPermissions) {
                if (pc.implies(permission)) {
                    counter.incrementAndGet();
                    return true;
                }
            }
        }
        if (r.nextInt(500) == 0) {
            System.out.println("Caught: "+counter.get());
            System.out.println("Not Caught: "+permission.getName());
        }
        return delegate.implies(permission);
    }

    @Override
    public Enumeration<Permission> elements() {
        return delegate.elements();
    }
}
