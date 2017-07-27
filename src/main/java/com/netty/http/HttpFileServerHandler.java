package com.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

/**
 * Created by ypc on 2017/7/26.
 */
public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String DEFAULT_URL = "/src/main/java/com/netty/";

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    @Override
    public void messageReceived(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        // 对 HTTP 请求消息的解码结果进行判断，失败直接构造 HTTP 400 错误返回
        if (!msg.getDecoderResult().isSuccess()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        // 对 HTTP 请求行中的方法进行判断，如果不是 GET 请求，直接构造 HTTP 405 错误返回
        if (msg.getMethod() != HttpMethod.GET) {
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        final String uri = msg.getUri();
        // sanitizeUri 得到系统 uri 绝对路径
        final String path = sanitizeUri(uri);

        // 如果构造的 URI 不合法，直接构造 HTTP 403 错误返回
        if (path == null) {
            sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }

        // 使用新组装的 URI 路径构造 File 对象，如果文件不存在或者是隐藏文件，直接构造 HTTP 404 错误返回
        File file = new File(path);
        if (file.isHidden() || !file.exists()) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        // 如果 file 是目录，则发送目录的链接给客户端浏览器。
        if (file.isDirectory()) {
            if (uri.endsWith("/")) {
                sendListing(ctx, file);
            } else {
                sendRedirect(ctx, uri + '/');
            }
            return;
        }

        // 如果 file 不是文件，直接构造 HTTP 403 错误返回
        if (!file.isFile()) {
            sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }

        // 使用随机文件读写类以只读方式打开文件（测试中只有360IE模式可以打开文件），
        // 如果文件打开失败，直接构造 HTTP 404 错误返回
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");// 以只读的方式打开文件
        } catch (FileNotFoundException fnfe) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        // 获取文件长度
        long fileLength = randomAccessFile.length();
        // 创建成功的 HTTP 相应消息
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        // 在响应消息头中设置 Content-Length 为 fileLength
        HttpHeaders.setContentLength(response, fileLength);
        // 在响应消息头中设置 Content-Type 为 mimeTypesMap.getContentType(file.getPath())
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));

        // 在响应消息头中设置 Connection 为 keep-alive
        if (HttpHeaders.isKeepAlive(msg)) {
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
        // 发送响应消息
        ctx.write(response);
        // 通过 Netty 的 ChunkedFile 对象直接将文件写入到缓存区中。
        ChannelFuture sendFileFuture =
                ctx.write(new ChunkedFile(randomAccessFile, 0, fileLength, 8192), ctx.newProgressivePromise());

        // 添加 GenericFutureListener 接口（多态），发送完成后打印 Transfer complete.
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                if (total < 0) { // total unknown
                    System.err.println("Transfer progress: " + progress);
                } else {
                    System.err.println("Transfer progress: " + progress + " / " + total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future)
                    throws Exception {
                System.out.println("Transfer complete.");
            }
        });
        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if (!HttpHeaders.isKeepAlive(msg)) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 返回系统 url 绝对路径
     * @param uri
     * @return
     */
    private String sanitizeUri(String uri) {
        // 使用 JDK 的 URLDecoder 对 URL 进行解码，使用 UTF-8 字符集，如果解析失败 使用 ISO-8859-1 解析
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }

        // 解码成功后对 URI 进行合法性判断，如果 RUI 与允许访问的 URI 一致或者是其子目录（文件），则校验通过，否则返回空。
        if (!uri.startsWith(DEFAULT_URL)) {
            return null;
        }
        if (!uri.startsWith("/")) {
            return null;
        }
        // 将硬编码的文件路径分配符替换为本地操作系统的文件路径分隔符
        uri = uri.replace('/', File.separatorChar);

        // 对新的 URI 做二次合法性校验，如果失败返回空。
        if (uri.contains(File.separator + '.')
                || uri.contains('.' + File.separator)
                || uri.startsWith(".")
                || uri.endsWith(".")
                || INSECURE_URI.matcher(uri).matches()) {
            return null;
        }

        // 对文件名进行拼接，使用当前运行程序所在的工程目录 + URI 构造绝对路径返回。
        //return System.getProperty("user.dir") + File.separator + uri;
        return "/Users/yangpengcheng/IdeaProjects/wo/netty" + File.separator + uri;
    }

    private static void sendListing(ChannelHandlerContext ctx, File dir) {
        // 创建成功的 HTTP 相应消息
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        // 设置消息头的类型为 "text/html; charset=UTF-8"
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
        // 构建消息体（拼接HTML）
        StringBuilder buf = new StringBuilder();
        String dirPath = dir.getPath();
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append(dirPath);
        buf.append(" 目录：");
        buf.append("</title></head><body>\r\n");
        buf.append("<h3>");
        buf.append(dirPath).append(" 目录：");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        // .. 链接 返回上一级
        buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
        // 用于展示根目录下的所有文件和文件夹，使用超链接来标识
        for (File f : dir.listFiles()) {
            if (f.isHidden() || !f.canRead()) {
                continue;
            }
            String name = f.getName();
            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                continue;
            }
            buf.append("<li>链接：<a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }
        buf.append("</ul></body></html>\r\n");
        // 分配对应消息的缓存对象
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        // 将缓存中的数据写到 HTTP 应答消息中
        response.content().writeBytes(buffer);
        // 释放缓存区
        buffer.release();
        // 调用 writeAndFlush 将相应消息发送到缓存区并刷新到 SocketChannel 中。
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.headers().set(HttpHeaders.Names.LOCATION, newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                status, Unpooled.copiedBuffer("Failure: " + status.toString()
                + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

}
