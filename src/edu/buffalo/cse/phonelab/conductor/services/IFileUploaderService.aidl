package edu.buffalo.cse.phonelab.conductor.services;

interface IFileUploaderService {

    void upload(String packageName, String path);

    boolean isUploaded(String path);
}
