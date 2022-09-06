package jpabook.jpashop.controller;

import jpabook.jpashop.Service.ItemService;
import jpabook.jpashop.Service.UpdateItemDto;
import jpabook.jpashop.domain.Item.Book;
import jpabook.jpashop.domain.Item.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm"; //form을 가져옴
    }

    @PostMapping("/items/new")
    public String create(BookForm form) { //form의 값을 전달받음
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        //setter 날리고 parameter로 생성하는 것이 더 좋다!!
        itemService.saveItem(book);
        return "redirect:/items"; //상품목록조회로 이동
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    @GetMapping("/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId);

        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setAuthor(item.getAuthor());
        form.setStockQuantity(item.getStockQuantity());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form", form);
        return "items/updateItemForm"; //이미 채워진 form을 가져옴
    }

    @PostMapping("/items/{itemId}/edit")
    public String updateItem(@ModelAttribute("form") UpdateItemDto updateItemDto) {

//            Book book = new Book();
//
//            book.setId(form.getId());
//            book.setName(form.getName());
//            book.setPrice(form.getPrice());
//            book.setStockQuantity(form.getStockQuantity());
//            book.setAuthor(form.getAuthor());
//            book.setIsbn(form.getIsbn());

        itemService.updateItem(updateItemDto.getId(), updateItemDto.getName(), updateItemDto.getPrice(), updateItemDto.getStockQuantity());
        return "redirect:/items";
    }
}