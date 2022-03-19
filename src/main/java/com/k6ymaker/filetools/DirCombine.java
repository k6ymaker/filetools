package com.k6ymaker.filetools;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DirCombine {

    public static void walk(String path,List<Path> addFilePathList) {
        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
                walk(f.getAbsolutePath(),addFilePathList);
            }
            else {
                if(f.getName().equals(".DS_Store")){
                    continue;
                }
                addFilePathList.add(Paths.get(f.getAbsolutePath()));
            }
        }
    }

    public static void combine(Path mainPath, Path addPath) throws Exception{
        List<Path> addFilePathList = new ArrayList<Path>();

        walk(addPath.toFile().getAbsolutePath(),addFilePathList);

        for(Path addFilePath : addFilePathList){

            Path relativePath = addPath.relativize(addFilePath);
            Path aimPath = mainPath.resolve(relativePath);

            if(aimPath.toFile().exists()){
                continue;
            }

            System.out.printf("copy from %s to %s\n",addFilePath,aimPath);

            if(!aimPath.getParent().toFile().exists()){
                aimPath.getParent().toFile().mkdirs();
            }

            Files.copy(addFilePath,aimPath);
        }
    }
}
