package kr.kernel360.anabada.domain.tradeoffer.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.kernel360.anabada.domain.member.entity.Member;
import kr.kernel360.anabada.domain.member.repository.MemberRepository;
import kr.kernel360.anabada.domain.trade.entity.Trade;
import kr.kernel360.anabada.domain.trade.repository.TradeRepository;
import kr.kernel360.anabada.domain.tradeoffer.dto.CreateTradeOfferRequest;
import kr.kernel360.anabada.domain.tradeoffer.dto.FindAllTradeOfferRequest;
import kr.kernel360.anabada.domain.tradeoffer.dto.FindAllTradeOfferResponse;
import kr.kernel360.anabada.domain.tradeoffer.dto.FindTradeOfferDto;
import kr.kernel360.anabada.domain.tradeoffer.dto.FindTradeOfferResponse;
import kr.kernel360.anabada.domain.tradeoffer.dto.UpdateTradeOfferRequest;
import kr.kernel360.anabada.domain.tradeoffer.dto.UpdateTradeOfferResponse;
import kr.kernel360.anabada.domain.tradeoffer.entity.TradeOffer;
import kr.kernel360.anabada.domain.tradeoffer.repository.TradeOfferRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeOfferService {
	private final TradeOfferRepository tradeOfferRepository;
	private final MemberRepository memberRepository;
	private final TradeRepository tradeRepository;

	public FindAllTradeOfferResponse findAll(FindAllTradeOfferRequest findAllTradeOfferRequest, Pageable pageable) {
		Page<FindTradeOfferDto> tradeOffers = tradeOfferRepository.findAll(findAllTradeOfferRequest, pageable);
		return FindAllTradeOfferResponse.of(tradeOffers);
	}

	public FindTradeOfferResponse find(Long tradeOfferId) {
		FindTradeOfferDto findTradeOfferDto = Optional.ofNullable(tradeOfferRepository.find(tradeOfferId))
			.orElseThrow(() -> new IllegalArgumentException("교환 요청이 존재하지 않습니다"));

		return FindTradeOfferResponse.of(findTradeOfferDto);
	}

	@Transactional
	public Long create(CreateTradeOfferRequest createTradeOfferRequest, Long tradeId) {
		String findEmailByJwt = SecurityContextHolder.getContext().getAuthentication().getName();
		Member member = memberRepository.findOneWithAuthoritiesByEmail(findEmailByJwt)
			.orElseThrow(()-> new IllegalArgumentException("멤버가 존재하지 않습니다"));

		Trade trade = findTradeById(tradeId);

		return tradeOfferRepository.save(CreateTradeOfferRequest.toEntity(createTradeOfferRequest, member, trade)).getId();
	}

	@Transactional
	public UpdateTradeOfferResponse update(UpdateTradeOfferRequest updateTradeOfferRequest) {
		TradeOffer tradeOffer = findTradeOfferById(updateTradeOfferRequest.getId());

		tradeOffer.update(updateTradeOfferRequest.getTitle(), updateTradeOfferRequest.getContent(),
			updateTradeOfferRequest.getImagePath());

		return UpdateTradeOfferResponse.of(tradeOffer);
	}

	@Transactional
	public void remove(Long tradeOfferId) {
		TradeOffer tradeOffer = findTradeOfferById(tradeOfferId);

		tradeOffer.remove();
	}

	private TradeOffer findTradeOfferById(Long id) {
		return tradeOfferRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("해당하는 교환 요청이 없습니다."));
	}


	private Trade findTradeById(Long tradeId) {
		return tradeRepository.findById(tradeId).orElseThrow(() -> new IllegalArgumentException(""));
	}
}
