pragma solidity ^0.5.10;

contract Ownable {
    address public owner;
    event OwnershipTransferred(address indexed _previousOwner, address indexed _newOwner);

    constructor() public {
        owner = msg.sender;
    }

    modifier onlyOwner() {
        require(msg.sender == owner);
        _;
    }

    function transferOwnership(address _newOwner) public onlyOwner {
        require(_newOwner != address(0));
        emit OwnershipTransferred(owner, _newOwner);
        owner = _newOwner;
    }
}

contract Token is Ownable {
    string public name;
    string public symbol;
    uint8 public decimals = 18;
    uint256 public totalSupply;
    mapping (address => uint256) public balanceOf;
    event Transfer(address indexed _from, address indexed _to, uint256 _value);
    event Mint(address indexed from, uint256 value);
    event Burn(address indexed from, uint256 value);
    
    constructor(
        string memory _name,
        string memory _symbol,
        uint256 _totalSupply) public {
        name = _name;
        symbol = _symbol;
        totalSupply = _totalSupply * (10 ** uint256(decimals));
        balanceOf[msg.sender] = totalSupply;
    }

    function transfer(address _to, uint256 _value) public returns (bool _success) {
        require(balanceOf[msg.sender] >= _value);
        require(balanceOf[_to] + _value >= balanceOf[_to]);
        require(_to != address(0));
        
        balanceOf[msg.sender] -= _value;
        balanceOf[_to] += _value;
        
        emit Transfer(msg.sender, _to, _value);
        
        return true;
    }

    function transferFrom(address _from, address _to, uint256 _value) public onlyOwner returns (bool _success) {
        require(balanceOf[_from] >= _value);
        require(balanceOf[_to] + _value >= balanceOf[_to]);
        require(_to != address(0));
        
        balanceOf[_from] -= _value;
        balanceOf[_to] += _value;
        
        emit Transfer(_from, _to, _value);
        
        return true;
    }
    
    function mint(uint256 _value) public onlyOwner returns (bool _success) {
        require(balanceOf[msg.sender] + _value >= balanceOf[msg.sender]);
        
        balanceOf[msg.sender] += _value;
        
        emit Mint(msg.sender, _value);
        
        return true;
    }
    
    function burn(uint256 _value) public onlyOwner returns (bool _success) {
        require(balanceOf[msg.sender] >= _value);
        
        balanceOf[msg.sender] -= _value;
        
        emit Burn(msg.sender, _value);
        
        return true;
    }
    
    function setName(string memory _newname) public onlyOwner returns (bool _success) {
        name = _newname;
        return true;
    }
    
    function setSymbol(string memory _newSymbol) public onlyOwner returns (bool _success) {
        symbol = _newSymbol;
        return true;
    }
}