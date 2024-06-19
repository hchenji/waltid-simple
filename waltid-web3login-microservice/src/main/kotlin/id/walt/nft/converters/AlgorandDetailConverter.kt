package id.walt.webwallet.service.nft.converters

import id.walt.nftkit.services.AlgorandToken
import id.walt.webwallet.service.dto.NftConvertResult
import id.walt.webwallet.service.dto.TokenArt

class AlgorandDetailConverter : NftDetailConverterBase<AlgorandToken>() {
    override fun convert(data: AlgorandToken): NftConvertResult = NftConvertResult(
        id = data.TokenParams?.index.toString(),
        name = data.TokenParams?.params?.name,
        description = data.Metadata?.description,
        art = TokenArt(url = data.Metadata?.image)
    )
}
