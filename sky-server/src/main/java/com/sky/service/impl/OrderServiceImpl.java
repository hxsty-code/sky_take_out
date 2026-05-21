package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional  // 开启事务
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //1、处理各种业务异常（地址簿为空、购物车数据为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            throw new RuntimeException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();   // 获取当前用户id
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if(shoppingCartList == null || shoppingCartList.size() == 0){
            throw new RuntimeException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //2、向订单表插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());

        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        //3、向订单明细表插入n条数据
        for(ShoppingCart cart : shoppingCartList){
            OrderDetail orderDetail = new OrderDetail();    // 创建一个订单明细对象
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());         // 设置当前订单明细关联的订单id
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //4、清空当前用户的购物车数据
        shoppingCartMapper.deleteByUserId(userId);
        //5、封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
        return orderSubmitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
//        // 当前登录用户id
//        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.getById(userId);
//
//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
//
//        return vo;
        log.info("跳过微信支付，支付成功");
        paySuccess(ordersPaymentDTO.getOrderNumber());
        return new OrderPaymentVO();
    }

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
     * 历史订单列表
     * @return
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {

        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        List<Orders> orderslist= orderMapper.pageQuery(ordersPageQueryDTO); //查询订单
        
        if (orderslist != null && !orderslist.isEmpty()) {
            List<Long> orderIds = orderslist.stream()
                    .map(Orders::getId)             // 获取订单id
                    .collect(Collectors.toList());  // 将订单id列表转换为流
            // 根据订单id批量查询订单详情
            List<OrderDetail> allOrderDetails = orderDetailMapper.listByOrderIds(orderIds);

                // 将订单详情根据订单id进行分组
                Map<Long, List<OrderDetail>> detailMap = allOrderDetails.stream()
                        .collect(Collectors.groupingBy(detail -> detail.getOrderId()));

            orderslist.forEach(order -> 
                order.setOrderDetailList(detailMap.getOrDefault(order.getId(), new ArrayList<>()))
            );
        }
        
        Page<Orders> page = (Page<Orders>) orderslist;
        return new PageResult(page.getTotal(), page.getResult());
    }
}
