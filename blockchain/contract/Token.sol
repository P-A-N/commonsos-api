pragma solidity ^0.5.4;

contract ERC20 {
    string public name;
    string public symbol;
    uint8 public decimals;
    uint256 public totalSupply;
    mapping (address => uint256) public balanceOf;
    mapping (address => mapping (address => uint256)) public allowance;
    function transfer(address _to, uint256 _value) public returns (bool _success);
    function transferFrom(address _from, address _to, uint256 _value) public returns (bool _success);
    function approve(address _spender, uint256 _value) public returns (bool _success);
    event Transfer(address indexed _from, address indexed _to, uint256 _value);
    event Approval(address indexed _owner, address indexed _spender, uint256 _value);
}

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

contract Token is ERC20, Ownable {
    event Mint(address indexed from, uint256 value);
    event Burn(address indexed from, uint256 value);
    
    constructor(
            string memory _name,
            string memory _symbol,
            uint256 _totalSupply) public {
        name = _name;
        symbol = _symbol;
        decimals = 18;
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

    function transferFrom(address _from, address _to, uint256 _value) public returns (bool _success) {
        require(allowance[_from][msg.sender] >= _value);
        require(balanceOf[_from] >= _value);
        require(balanceOf[_to] + _value >= balanceOf[_to]);
        require(_to != address(0));
        
        allowance[_from][msg.sender] -= _value;
        balanceOf[_from] -= _value;
        balanceOf[_to] += _value;
        
        emit Transfer(_from, _to, _value);
        
        return true;
    }

    function approve(address _spender, uint256 _value) public returns (bool _success) {
        allowance[msg.sender][_spender] = _value;
        emit Approval(msg.sender, _spender, _value);
        return true;
    }
    
    function mint(uint256 _value) public onlyOwner returns (bool _success) {
        require(balanceOf[msg.sender] + _value >= balanceOf[msg.sender]);
        require(totalSupply + _value >= totalSupply);
        
        balanceOf[msg.sender] += _value;
        totalSupply += _value;
        
        emit Mint(msg.sender, _value);
        
        return true;
    }
    
    function burn(uint256 _value) public onlyOwner returns (bool _success) {
        require(balanceOf[msg.sender] >= _value);
        require(totalSupply >= _value);
        
        balanceOf[msg.sender] -= _value;
        totalSupply -= _value;
        
        emit Burn(msg.sender, _value);
        
        return true;
    }
    
    function setName(string memory _name) public onlyOwner returns (bool _success) {
        name = _name;
        return true;
    }

    function setSymbol(string memory _symbol) public onlyOwner returns (bool _success) {
        symbol = _symbol;
        return true;
    }
}