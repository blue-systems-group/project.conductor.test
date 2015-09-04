package edu.buffalo.cse.phonelab.harness.lib.services;

interface IFileUploaderService {

    void upload(String packageName, String path);

    boolean isUploaded(String path);
}
