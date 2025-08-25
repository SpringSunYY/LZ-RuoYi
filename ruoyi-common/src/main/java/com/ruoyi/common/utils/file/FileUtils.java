package com.ruoyi.common.utils.file;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * 文件处理工具类
 *
 * @author ruoyi
 */
@Slf4j
public class FileUtils
{
    public static String FILENAME_PATTERN = "[a-zA-Z0-9_\\-\\|\\.\\u4e00-\\u9fa5]+";

    /**
     * 输出指定文件的byte数组
     *
     * @param filePath 文件路径
     * @param os 输出流
     * @return
     */
    public static void writeBytes(String filePath, OutputStream os) throws IOException
    {
        FileInputStream fis = null;
        try
        {
            File file = new File(filePath);
            if (!file.exists())
            {
                throw new FileNotFoundException(filePath);
            }
            fis = new FileInputStream(file);
            byte[] b = new byte[1024];
            int length;
            while ((length = fis.read(b)) > 0)
            {
                os.write(b, 0, length);
            }
        }
        catch (IOException e)
        {
            throw e;
        }
        finally
        {
            IOUtils.close(os);
            IOUtils.close(fis);
        }
    }

    /**
     * 写数据到文件中
     *
     * @param data 数据
     * @return 目标文件
     * @throws IOException IO异常
     */
    public static String writeImportBytes(byte[] data) throws IOException
    {
        return writeBytes(data, RuoYiConfig.getImportPath());
    }

    /**
     * 写数据到文件中
     *
     * @param data 数据
     * @param uploadDir 目标文件
     * @return 目标文件
     * @throws IOException IO异常
     */
    public static String writeBytes(byte[] data, String uploadDir) throws IOException
    {
        FileOutputStream fos = null;
        String pathName = "";
        try
        {
            String extension = getFileExtendName(data);
            pathName = DateUtils.datePath() + "/" + IdUtils.fastUUID() + "." + extension;
            File file = FileUploadUtils.getAbsoluteFile(uploadDir, pathName);
            fos = new FileOutputStream(file);
            fos.write(data);
        }
        finally
        {
            IOUtils.close(fos);
        }
        return FileUploadUtils.getPathFileName(uploadDir, pathName);
    }

    /**
     * 删除文件
     *
     * @param filePath 文件
     * @return
     */
    public static boolean deleteFile(String filePath)
    {
        boolean flag = false;
        File file = new File(filePath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists())
        {
            flag = file.delete();
        }
        return flag;
    }

    /**
     * 文件名称验证
     *
     * @param filename 文件名称
     * @return true 正常 false 非法
     */
    public static boolean isValidFilename(String filename)
    {
        return filename.matches(FILENAME_PATTERN);
    }

    /**
     * 检查文件是否可下载
     *
     * @param resource 需要下载的文件
     * @return true 正常 false 非法
     */
    public static boolean checkAllowDownload(String resource)
    {
        // 禁止目录上跳级别
        if (StringUtils.contains(resource, ".."))
        {
            return false;
        }

        // 检查允许下载的文件规则
        if (ArrayUtils.contains(MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION, FileTypeUtils.getFileType(resource)))
        {
            return true;
        }

        // 不在允许下载的文件规则
        return false;
    }

    /**
     * 下载文件名重新编码
     *
     * @param request 请求对象
     * @param fileName 文件名
     * @return 编码后的文件名
     */
    public static String setFileDownloadHeader(HttpServletRequest request, String fileName) throws UnsupportedEncodingException
    {
        final String agent = request.getHeader("USER-AGENT");
        String filename = fileName;
        if (agent.contains("MSIE"))
        {
            // IE浏览器
            filename = URLEncoder.encode(filename, "utf-8");
            filename = filename.replace("+", " ");
        }
        else if (agent.contains("Firefox"))
        {
            // 火狐浏览器
            filename = new String(fileName.getBytes(), "ISO8859-1");
        }
        else if (agent.contains("Chrome"))
        {
            // google浏览器
            filename = URLEncoder.encode(filename, "utf-8");
        }
        else
        {
            // 其它浏览器
            filename = URLEncoder.encode(filename, "utf-8");
        }
        return filename;
    }

    /**
     * 下载文件名重新编码
     *
     * @param response 响应对象
     * @param realFileName 真实文件名
     */
    public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName) throws UnsupportedEncodingException
    {
        String percentEncodedFileName = percentEncode(realFileName);

        StringBuilder contentDispositionValue = new StringBuilder();
        contentDispositionValue.append("attachment; filename=")
                .append(percentEncodedFileName)
                .append(";")
                .append("filename*=")
                .append("utf-8''")
                .append(percentEncodedFileName);

        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
        response.setHeader("Content-disposition", contentDispositionValue.toString());
        response.setHeader("download-filename", percentEncodedFileName);
    }

    /**
     * 百分号编码工具方法
     *
     * @param s 需要百分号编码的字符串
     * @return 百分号编码后的字符串
     */
    public static String percentEncode(String s) throws UnsupportedEncodingException
    {
        String encode = URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        return encode.replaceAll("\\+", "%20");
    }

    /**
     * 获取图像后缀
     *
     * @param photoByte 图像数据
     * @return 后缀名
     */
    public static String getFileExtendName(byte[] photoByte)
    {
        String strFileExtendName = "jpg";
        if ((photoByte[0] == 71) && (photoByte[1] == 73) && (photoByte[2] == 70) && (photoByte[3] == 56)
                && ((photoByte[4] == 55) || (photoByte[4] == 57)) && (photoByte[5] == 97))
        {
            strFileExtendName = "gif";
        }
        else if ((photoByte[6] == 74) && (photoByte[7] == 70) && (photoByte[8] == 73) && (photoByte[9] == 70))
        {
            strFileExtendName = "jpg";
        }
        else if ((photoByte[0] == 66) && (photoByte[1] == 77))
        {
            strFileExtendName = "bmp";
        }
        else if ((photoByte[1] == 80) && (photoByte[2] == 78) && (photoByte[3] == 71))
        {
            strFileExtendName = "png";
        }
        return strFileExtendName;
    }

    /**
     * 获取文件名称 /profile/upload/2022/04/16/ruoyi.png -- ruoyi.png
     *
     * @param fileName 路径名称
     * @return 没有文件路径的名称
     */
    public static String getName(String fileName)
    {
        if (fileName == null)
        {
            return null;
        }
        int lastUnixPos = fileName.lastIndexOf('/');
        int lastWindowsPos = fileName.lastIndexOf('\\');
        int index = Math.max(lastUnixPos, lastWindowsPos);
        return fileName.substring(index + 1);
    }

    /**
     * 获取不带后缀文件名称 /profile/upload/2022/04/16/ruoyi.png -- ruoyi
     *
     * @param fileName 路径名称
     * @return 没有文件路径和后缀的名称
     */
    public static String getNameNotSuffix(String fileName)
    {
        if (fileName == null)
        {
            return null;
        }
        return FilenameUtils.getBaseName(fileName);
    }

    /**
     * 判断文件是否存在
     */
    public static boolean isFileExists(String filePath) {
        return new File(filePath).exists();
    }

    /**
     * 压缩文件成zip
     *
     * @param files       文件列表
     * @param zipFilePath 压缩文件路径
     * @return zipPath
     * @author: YY
     * @method: compressZip
     * @date: 2025/7/25 19:11
     **/
    public static String compressZip(List<String> files, String zipFilePath) {
        if (files == null || files.isEmpty()) {
            log.error("文件列表不能为空");
            return null;
        }

        if (StringUtils.isEmpty(zipFilePath)) {
            log.error("压缩文件路径不能为空");
            return null;
        }

        FileOutputStream fos = null;
        ZipOutputStream zos = null;

        try {
            // 创建ZIP文件
            File zipFile = new File(zipFilePath);
            // 确保父目录存在
            File parentDir = zipFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    log.error("创建父目录失败: {}", parentDir.getAbsolutePath());
                    return null;
                }
            }

            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);

            // 遍历文件列表，逐个添加到ZIP中
            for (String filePath : files) {
                if (StringUtils.isEmpty(filePath)) {
                    continue;
                }

                File file = new File(filePath);
                if (!file.exists()) {
                    log.error("文件或目录不存在: {}", filePath);
                    continue;
                }

                // 递归处理文件或目录
                addFileToZip(zos, file, "");
            }

            return zipFilePath;
        } catch (IOException e) {
            throw new RuntimeException("压缩文件失败: " + e.getMessage(), e);
        } finally {
            try {
                IOUtils.close(zos);
                IOUtils.close(fos);
            } catch (IOException e) {
                log.error("关闭ZIP流失败: " + e.getMessage());
            }
        }
    }

    /**
     * 递归添加文件或目录到ZIP
     *
     * @param zos      ZIP输出流
     * @param file     要添加的文件或目录
     * @param basePath 在ZIP中的基础路径
     * @throws IOException IO异常
     */
    private static void addFileToZip(ZipOutputStream zos, File file, String basePath) throws IOException {
        if (file.isFile()) {
            // 处理文件
            log.info("正在添加文件: {}", file.getAbsolutePath());
            String entryName = basePath + file.getName();
            ZipEntry zipEntry = new ZipEntry(entryName);
            zos.putNextEntry(zipEntry);

            // 读取文件内容并写入ZIP
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
            } finally {
                IOUtils.close(fis);
            }

            // 关闭当前条目
            zos.closeEntry();
        } else if (file.isDirectory()) {
            // 处理目录
            log.info("正在处理目录: {}", file.getAbsolutePath());
            String entryName = basePath + file.getName() + "/";

            // 添加目录条目
            ZipEntry zipEntry = new ZipEntry(entryName);
            zos.putNextEntry(zipEntry);
            zos.closeEntry();

            // 递归处理目录下的所有文件和子目录
            File[] files = file.listFiles();
            if (files != null) {
                for (File childFile : files) {
                    addFileToZip(zos, childFile, entryName);
                }
            }
        }
    }

    /**
     * 根据文件路径读取文件内容
     */
    public static String readFileContent(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            log.error("文件路径不能为空");
            return null;
        }
        try (FileInputStream fis = new FileInputStream(filePath);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, length);
            }
            return bos.toString();
        } catch (IOException e) {
            log.error("读取文件内容失败", e);
            return null;
        }
    }

    /**
     * 获取文件后缀名
     *
     * @param fileName 文件名
     * @return
     */
    public static String getSuffixName(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        fileName = fileName.substring(lastDotIndex + 1);
        //判断是否有?
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        return fileName;
    }
}
