package com.example;

import com.example.LibraryManager;

/**
 * Interface implemented by all screen controllers that need
 * access to the shared LibraryManager instance.
 * CM1601 - Library Management System
 */
public interface ManagerAware {

    void setManager(LibraryManager manager);
}
