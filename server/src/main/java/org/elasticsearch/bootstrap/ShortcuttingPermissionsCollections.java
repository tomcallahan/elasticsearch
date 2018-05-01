package org.elasticsearch.bootstrap;

import org.elasticsearch.common.SuppressForbidden;

import java.io.FilePermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressForbidden(reason = "whatever")
public class ShortcuttingPermissionsCollections extends PermissionCollection {
    private final CopyOnWriteArrayList<PermissionCollection> perms = new CopyOnWriteArrayList<>();
    private final PermissionCollection delegate;
    int permNo = 0;
    int delegateNo = 0;
    Random r = new Random();
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
            permNo++;
        }
        delegate.add(permission);
        delegateNo++;

        System.out.println("shortcut perms: "+permNo);
        System.out.println("delegate num: "+delegateNo);
    }

    @Override
    public boolean implies(Permission permission) {
        int x=0;
        if (permission instanceof FilePermission) {
            for (PermissionCollection pc : perms) {
                x++;
                if (pc.implies(permission)) {
                    if (r.nextInt(50) == 0) {
                        System.out.println("checks: "+x);
                    }
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
