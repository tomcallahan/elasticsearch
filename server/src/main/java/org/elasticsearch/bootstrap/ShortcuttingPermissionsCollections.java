package org.elasticsearch.bootstrap;

import org.elasticsearch.common.SuppressForbidden;

import java.io.FilePermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SuppressForbidden(reason = "whatever")
public class ShortcuttingPermissionsCollections extends PermissionCollection {
    private static final int THRESHOLD = 10_000;
    private volatile int accesses = 0;
    private volatile CopyOnWriteArrayList<PermissionCollectionContainer> perms = new CopyOnWriteArrayList<>();
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
            PermissionCollectionContainer pcc = new PermissionCollectionContainer(pc);
            perms.add(pcc);
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
        accesses++;
        if (accesses == THRESHOLD) {
            reorder();
        }
        if (permission instanceof FilePermission) {
            for (PermissionCollectionContainer pcc : perms) {
                x++;
                if (pcc.perm.implies(permission)) {
                    pcc.accesses.incrementAndGet();
                    if (r.nextInt(50) == 0) {
                        System.out.println("checks: "+x+ " " + permission.getName());
                    }
                    return true;
                }
            }
        }
        return delegate.implies(permission);
    }
    private void reorder() {
        List<PermissionCollectionContainer> list = perms.stream()
            .sorted((a, b) -> b.accesses.get() - a.accesses.get()).collect(Collectors.toList());
        perms = new CopyOnWriteArrayList<>(list);
        System.out.println("re-ordered");
    }

    @Override
    public Enumeration<Permission> elements() {
        return delegate.elements();
    }

    static class PermissionCollectionContainer {
        public PermissionCollectionContainer(PermissionCollection perm) {
            this.perm = perm;
        }
        private final PermissionCollection perm;
        private final AtomicInteger accesses = new AtomicInteger(0);
    }
}
