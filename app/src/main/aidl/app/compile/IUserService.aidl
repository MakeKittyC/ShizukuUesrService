package app.compile;
/*
   源代码开源是为了让您观摩我的诗山代码，而不是教您如何使用 Shizuku-13.1.5 版本的依赖
*/
interface IUserService {

    /**
     * Shizuku服务端定义的销毁方法
     */
    void destroy() = 16777114;

    /**
     * 自定义的退出方法
     */
    void exit() = 1;

    /**
     * 执行命令
     */
    String execLine(String command) = 2;

    /**
     * 执行数组中分离的命令
     */
    String execArr(in String[] command) = 3;
}


