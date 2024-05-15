package jp.co.metateam.library.controller;
import java.util.List;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.validation.BindingResult;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.service.BookMstService;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.Stock;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.FieldError;
import java.util.Optional;
//import jp.co.metateam.library.constants.Constants

/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {

    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;
    private final BookMstService bookMstService;
    @Autowired
    public RentalManageController(
        AccountService accountService, 
        RentalManageService rentalManageService, 
        StockService stockService,
        BookMstService bookMstService
    ) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
        this.bookMstService = bookMstService;
    }

    /**
     * 貸出一覧画面初期表示
     * @param model
     * @return
     */
    @GetMapping("/rental/index") 
    public String index(Model model) {
     //貸出管理テーブルから全取得
     List<RentalManage> rentalManageList = this.rentalManageService.findAll();
     //貸出一覧画面に渡すデータをmodelに追加
     model.addAttribute("rentalManageList", rentalManageList);
     //貸出一覧画面に遷移
     return "rental/index";
    }

    @GetMapping("/rental/add")
    public String add(Model model, @ModelAttribute RentalManageDto rentalManageDto) {
        
        List<Account> accounts = this.accountService.findAll();
        List <Stock> stockList = this.stockService.findAll();
        
        model.addAttribute("accounts",accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("RentalManageDto")) {
           model.addAttribute("RentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }



    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra, Model model) {
        try {
            String adderror;
            String stockId = rentalManageDto.getStockId();
            Long addsum = this.rentalManageService.countByStockIdAndStatusIn(stockId);
            Integer status = rentalManageDto.getStatus();
            if (status == 0 || status == 1) {
                
            if (!(addsum == 0)) {
                Date expectedRentalOn = rentalManageDto.getExpectedRentalOn();
                Date expectedReturnOn = rentalManageDto.getExpectedReturnOn();
                Long addnum = this.rentalManageService.countByStatusAndExpectedReturnBefore(stockId, expectedRentalOn, expectedReturnOn);
                if (!(addsum == addnum)) {
                    adderror = "この期間は貸出できません";
                    result.addError(new FieldError("rentalManageDto","expectedRentalOn", adderror));
                    result.addError(new FieldError("rentalManageDto","expectedReturnOn", adderror));
                }
            }
        }

            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            // 登録処理
            this.rentalManageService.save(rentalManageDto);

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            List<Account> accounts = this.accountService.findAll();
            List <Stock> stockList = this.stockService.findStockAvailableAll();
        
            model.addAttribute("accounts",accounts);
            model.addAttribute("stockList", stockList);
            model.addAttribute("rentalStatus", RentalStatus.values());

            return "/rental/add";
        }
    }
    
    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model) {

        List<Account> accounts = this.accountService.findAll();
        List<Stock> stockList= this.stockService.findStockAvailableAll();
        
        model.addAttribute("accounts",accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("RentalManageDto")) {
           model.addAttribute("RentalManageDto", new RentalManageDto());
        }

       
       //DBから編集する貸出管理番号のレコードを取得する
        RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
       
       //レコードのデータを挿入する箱を作る
     if (!model.containsAttribute("retalManageDto")) {
        RentalManageDto rentalManageDto = new RentalManageDto();
       //箱にデータを移す
       rentalManageDto.setId(rentalManage.getId());
       rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
       rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
       rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
       rentalManageDto.setStockId(rentalManage.getStock().getId());
       rentalManageDto.setStatus(rentalManage.getStatus());
        
       model.addAttribute("rentalManageDto", rentalManageDto);

     }
       
        
        
        return "rental/edit";

    }
    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra, Model model) {
        try {
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
            String rentalerror;
            //貸出ステータスチェック（編集前の貸出ステータスと編集後の貸出ステータスを呼び出しrentalmanageDtoでチェック）
            Optional<String> validErrorOptional = rentalManageDto.isStatusError(rentalManage.getStatus());
            // Optionalが空でない場合のみエラーを処理する
             validErrorOptional.ifPresent(validError -> {
                if (!validError.isEmpty()) {
                   result.addError(new FieldError("rentalmanageDto", "status", validError));
                    
                }
                });
            //貸出可否チェック（repositoryでDBにSQLを送り、その情報でチェック）
            Long rentalId = rentalManage.getId();
            String stockId = rentalManageDto.getStockId();
            Integer status = rentalManageDto.getStatus();
            if (status == 0 || status == 1) {
           Long rentalSum = this.rentalManageService.countByStatusAndNotId(rentalId,stockId);
            

            if (!(rentalSum == 0)) {
                Date expectedRentalOn = rentalManageDto.getExpectedRentalOn();
                Date expectedReturnlOn = rentalManageDto.getExpectedReturnOn();
             Long rentalNum = this.rentalManageService.countRecords(rentalId,stockId,expectedRentalOn,expectedReturnlOn);
             
            if (!(rentalSum == rentalNum)) {
                rentalerror = "この期間は貸出できません";
                result.addError(new FieldError("retanlmanageDto", "expectedRentalOn",rentalerror));
                result.addError(new FieldError("retanlmanageDto", "expectedReturnOn",rentalerror));
            }
            } }
        
           
        

        
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }

            // 更新処理
            this.rentalManageService.update(Long.valueOf(id), rentalManageDto);
            
            return "redirect:/rental/index";
        
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);
            //元の貸出データを取得
            List<Account> accounts = this.accountService.findAll();
            List <Stock> stockList = this.stockService.findStockAvailableAll();
        
            model.addAttribute("accounts",accounts);
            model.addAttribute("stockList", stockList);
            model.addAttribute("rentalStatus", RentalStatus.values());

       
        

            return "rental/edit";
        }
    }


}    
  
        
