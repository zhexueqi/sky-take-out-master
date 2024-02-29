package com.sky.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhexueqi
 * @ClassName OrderServiceImpl
 * @since 2024/2/28    16:07
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.baidu.ak}")
    private String ak;


    public static final String url = "https://api.map.baidu.com/geocoding/v3";

    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //先处理各种业务异常(例如，购物车空的，地址簿空的)
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null){
            //抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //判断用户地址是否在配送范围内
        String userAddress = addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail();
        checkOutOfRange(userAddress);

        //获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        //赋值
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        //查询对应用户的购物车信息
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0){
            //抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表插入一条数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        order.setOrderTime(LocalDateTime.now());
        order.setPhone(addressBook.getPhone());
        order.setPayStatus(Orders.UN_PAID);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setConsignee(addressBook.getConsignee());
        order.setUserId(userId);
        orderMapper.insert(order);

        //向订单明细表中插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            //在新增订单的时候，返回了主键值到order对象中
            orderDetail.setOrderId(order.getId());
            //存在list集合中，批量添加
            orderDetailList.add(orderDetail);
        }
        //批量插入数据
        orderDetailMapper.insertBatch(orderDetailList);

        //清空购物车数据
        shoppingCartMapper.deleteByUserId(shoppingCart);

        //构建OrderSubmitVO对象
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime()).build();



        return orderSubmitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );


        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 历史订单
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    public PageResult historyOrders(Integer page, Integer pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        // // 分页条件查询
        Page<Orders> result = orderMapper.pageQuery(ordersPageQueryDTO);

        List<Orders> ordersList = new ArrayList<>();

        if (result !=null && result.getTotal()>0){
            for (Orders order : result) {
                //获取订单id
                Long id = order.getId();
                //根据订单id查询订单详细
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                //将订单对象添加到集合中
                ordersList.add(orderVO);
            }
        }
        PageResult pageResult = new PageResult(result.getTotal(),ordersList);
        return pageResult;



    }

    /**
     * 订单详情
     * @param id
     * @return
     */
    public OrderVO getOrderDetail(Long id) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();

        Orders ordersResult = orderMapper.getById(id);
        BeanUtils.copyProperties(ordersResult, orderVO);

        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    /**
     * 取消订单
     * @param id
     */
    public void cancel(Long id) {
        Orders orders = Orders.builder()
                        .status(Orders.CANCELLED)
                        .id(id).build();

        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     */
    public void repetition(Long id) {
        //获取该订单的菜品详细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        //获取用户id
        Long userId = BaseContext.getCurrentId();
        //遍历订单的菜品，将每个菜品重新加入购物车
        if (orderDetailList !=null && orderDetailList.size()>0) {
            for (OrderDetail orderDetail : orderDetailList) {
                //构造对象
                ShoppingCart shoppingCart = ShoppingCart.builder()
                        .userId(userId)
                        .name(orderDetail.getName())
                        .dishId(orderDetail.getDishId())
                        .setmealId(orderDetail.getSetmealId())
                        .dishFlavor(orderDetail.getDishFlavor())
                        .number(orderDetail.getNumber())
                        .amount(orderDetail.getAmount())
                        .image(orderDetail.getImage())
                        .createTime(LocalDateTime.now())
                        .build();
                //重新加入购物车
                shoppingCartMapper.insert(shoppingCart);
            }
        }
    }

    /**
     * 条件查询
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());


        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> orderVOList = getOrderVOList(page);

        PageResult pageResult = new PageResult(page.getTotal(),orderVOList);
        return pageResult;
    }


    /**
     * 封装订单对象
     * @param page
     * @return
     */
    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> result = page.getResult();

        if (result !=null && result.size()>0){
            for (Orders orders : result) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 获取订单菜品字符串
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        List<String> DishesStrList = orderDetailList.stream().map(orderDetail -> {
            String DishesStr = orderDetail.getName() + "*" + orderDetail.getNumber() + ";";
            return DishesStr;
        }).collect(Collectors.toList());

        return String.join("",DishesStrList);
    }

    /**
     * 订单状态统计
     * @return
     */
    public OrderStatisticsVO getStatistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();

        Integer confirmed =orderMapper.getStatistics(Orders.CONFIRMED);
        Integer toBeConfirmed =orderMapper.getStatistics(Orders.TO_BE_CONFIRMED);
        Integer deliveryInProgress =orderMapper.getStatistics(Orders.DELIVERY_IN_PROGRESS);

        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 接单
     * @param orders
     */
    public void confirm(Orders orders) {
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }



    /**
     * 拒绝接单
     * @param orders
     */
    public void rejection(Orders orders) throws Exception {
        Orders ordersDB = orderMapper.getById(orders.getId());
        if (ordersDB.getStatus() != Orders.TO_BE_CONFIRMED) {
            throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if (ordersDB.getPayStatus() == Orders.PAID){
            String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("申请退款：{}", refund);
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 订单配送
     * @param id
     */
    public void delivery(Long id) {
        Orders orders = orderMapper.getById(id);
        if (!orders.getStatus().equals(Orders.CONFIRMED)){
            //如果订单状态不为待确认，则抛出业务异常
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //修改订单状态
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    /**
     * 订单完成
     * @param id
     */
    public void complete(Long id) {
        Orders orders = orderMapper.getById(id);
        //如果订单状态不为派送中，则抛出业务异常
        if (orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
    }

    /**
     * 检查地址是否在配送范围内
     * @param address 收货地址
     */
    public void checkOutOfRange(String address){
        HashMap map = new HashMap<>();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);
        //获取店铺的经纬度
        String shopCoordinate = HttpClientUtil.doGet(url, map);
        log.info("店铺坐标：{}",shopCoordinate);
        System.out.println(shopCoordinate);
        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        //判断店铺坐标是否解析成功,0为成功
        if (!jsonObject.getString("status").equals("0")){
            //要是解析失败，则抛出异常
            throw new OrderBusinessException(MessageConstant.SHOP_ADDRESS_ERROR);
        }

        //获取店铺经纬度信息
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String  lng = location.getString("lng");
        String  lat = location.getString("lat");
        String origin = lat + "," + lng;
        map.put("origin",origin);


        map.put("address",address);
        String userCoordinate = HttpClientUtil.doGet(url, map);
        log.info("用户坐标：{}",userCoordinate);
        JSONObject userJsonObject = JSON.parseObject(userCoordinate);
        //判断用户坐标是否解析成功,0为成功
        if (!userJsonObject.getString("status").equals("0")){
            //要是解析失败，则抛出异常
            throw new OrderBusinessException(MessageConstant.USER_ADDRESS_ERROR);
        }
        location = userJsonObject.getJSONObject("result").getJSONObject("location");
        lng = location.getString("lng");
        lat = location.getString("lat");
        String destination = lat + "," + lng;
        map.put("destination",destination);
        map.put("steps_info","0");


        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);
        log.info("路线规划：{}", json);
        JSONObject object = JSON.parseObject(json);
        if (!object.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }
        JSONObject result = object.getJSONObject("result");
        JSONArray routes = (JSONArray) result.get("routes");
        Integer distance = routes.getJSONObject(0).getInteger("distance");

        if (distance>5000){
            throw new OrderBusinessException("超出配送范围");
        }

    }

}
