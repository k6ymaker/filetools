package com.k6ymaker.filetools;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/*
 * 用来对比两个目录下文件差异
 * - 两个dir中共有的文件
 *    - hash相同文件
 *    - hash不同文件
 * - 单独有的文件
 *    - a中单独有的文件
 *    - b中单独有的文件
 * - 单独有的目录
 *    - a中单独有的目录
 *    - b中单独有的目录
 * */

public class MD5Compare {
    private String[] blackList;

    public MD5Compare(String[] blockList){
        this.blackList = blockList;
    }

    public void diff(Path a, Path b){
        List<Path> aPaths = new ArrayList<>();
        List<Path> bPaths = new ArrayList<>();


        walk(a,aPaths);
        walk(b,bPaths);

        System.out.printf("[%s] 总计%s\n",a.toString(),aPaths.size());
        System.out.printf("[%s] 总计%s\n",b.toString(),bPaths.size());
        System.out.println("\n\n");

        List<Path> abRelativePaths = new ArrayList<>();
        List<Path> aOnlyPaths = new ArrayList<>();
        List<Path> bOnlyPaths = new ArrayList<>();

        //提取a中独有文件、a及b都有文件
        for(Path ap:aPaths){
            boolean inB = false;
            Path apRelativePath = a.relativize(ap);

            for(Path bp:bPaths){
                Path bpRelativePath = b.relativize(bp);
                if(apRelativePath.equals(bpRelativePath)){
                    inB = true;
                    break;
                }
            }

            if(inB){
                abRelativePaths.add(apRelativePath);
            } else {
                aOnlyPaths.add(ap);
            }
        }

        //提取b中独有文件
        for(Path bp:bPaths){
            boolean inA = false;
            Path bpRelativePath = b.relativize(bp);

            for(Path abrp:abRelativePaths){
                if(bpRelativePath.equals(abrp)){
                    inA = true;
                    break;
                }
            }

            if(!inA){
                bOnlyPaths.add(bp);
            }
        }

        //对比md5
        List<Path> diffMd5RelativePaths = new ArrayList<>();

        for(Path abrp:abRelativePaths){
            Path pa = a.resolve(abrp);
            Path pb = b.resolve(abrp);

            String paMD5 = CalacFileMd5.getFileMD5(pa.toFile());
            String pbMD5 = CalacFileMd5.getFileMD5(pb.toFile());

            if(paMD5.equals(pbMD5)){
                continue;
            }

            //展示
            printDiffMD5(abrp,pa,paMD5,pb,pbMD5);
            diffMd5RelativePaths.add(abrp);
        }

        //输出a、b中单独有的文件
        printOnlyInOne(aOnlyPaths,a);
        printOnlyInOne(bOnlyPaths,b);
    }

    private void printDiffMD5(Path relativePath,Path a,String md5a,Path b,String md5b){
        System.out.printf("%s \t(%s,%s) - (%s,%s)\n",relativePath,a,md5a,b,md5b);
    }

    private void printOnlyInOne(List<Path> pathList,Path p){
        System.out.println("\n\n");
        System.out.printf("只在 [%s]中存在文件\n",p);
        for(Path pp:pathList){
            System.out.println(pp);
        }
    }

    private boolean inBlack(Path p){
        for(String bf:blackList){
            if(p.toFile().getName().equals(bf)){
                return true;
            }
        }

        return false;
    }

    private void walk(Path path,List<Path> paths) {
        File root = path.toFile();
        File[] list = root.listFiles();

        if(list == null) return;

        for(File f:list){
            if(f.isDirectory()){
                walk(Paths.get(f.toURI()),paths);
            } else{
                if(inBlack(f.toPath())){
                    continue;
                }
                paths.add(f.toPath());
            }
        }
    }
}
